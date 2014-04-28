package com.salesforce.perfeng.imageoptimization.web.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.salesforce.perfeng.imageoptimization.web.domain.FileMeta;
import com.salesforce.perfeng.imageoptimization.web.domain.SimpleOptimizationResult;
import com.salesforce.perfeng.uiperf.imageoptimization.dto.OptimizationResult;
import com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService;
import com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.FileTypeConversion;

@Controller
public final class ImageFileController {

	private final IImageOptimizationService<Object> imageOptimizationService;

	private final String tmpDirPath;

	/**
	 * Constructor
	 * 
	 * @param imageOptimizationService The service used to optimize the images
	 * @throws IOException Thrown if there is an issue creating the temp 
	 *                     directories.
	 */
	@Autowired
	public ImageFileController(final IImageOptimizationService<Object> imageOptimizationService) throws IOException {
		this.imageOptimizationService = imageOptimizationService;
		final File tmpDir = File.createTempFile(ImageFileController.class.getName(), "");
		tmpDir.delete();
		tmpDir.mkdir();
		tmpDirPath = tmpDir.getAbsolutePath() + "/";
	}

	@RequestMapping(value="upload", produces="application/json", method=RequestMethod.POST)
	@ResponseStatus(value=HttpStatus.OK)
	protected @ResponseBody Map<String, List<SimpleOptimizationResult>> upload(@RequestParam("conversion") final FileTypeConversion conversion, @RequestParam(value="webp", defaultValue="false") final boolean generateWebp, final MultipartHttpServletRequest request, final HttpServletResponse response) throws Exception {

		final List<File> files = new ArrayList<File>();

		//1. build an iterator
		final Iterator<String> itr =  request.getFileNames();
		MultipartFile mpf;

		FileMeta fileMeta;

		Map<String, Long> idMap = new HashMap<String, Long>();

		//2. get each file
		while(itr.hasNext()){

			//2.1 get next MultipartFile
			mpf = request.getFile(itr.next());

			//2.3 create new fileMeta
			fileMeta = new FileMeta();
			fileMeta.setFileName(mpf.getOriginalFilename());
			fileMeta.setFileSize(mpf.getSize() / 1024 + " Kb");
			fileMeta.setFileType(mpf.getContentType());

			try {
				fileMeta.setBytes(mpf.getBytes());

				final long id = System.nanoTime();
				
				new File(tmpDirPath + id + "/").mkdir();
				// copy file to local disk (make sure the path "e.g. D:/temp/files" exists)            
				FileCopyUtils.copy(mpf.getBytes(), new FileOutputStream(tmpDirPath + id + "/" + mpf.getOriginalFilename()));
				//2.4 add to files
				files.add(new File(tmpDirPath + id + "/" + mpf.getOriginalFilename()));
				
				idMap.put(mpf.getOriginalFilename(), Long.valueOf(id));
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// result will be like this
		// [{"fileName":"app_engine-85x77.png","fileSize":"8 Kb","fileType":"image/png"},...]

		final Map<String, List<SimpleOptimizationResult>>  results = convertOptimizationResultsToSimpleOptimizationResultsMap(imageOptimizationService.optimizeAllImages(conversion, generateWebp, files), idMap);

		return results;
	}

	private Map<String, List<SimpleOptimizationResult>> convertOptimizationResultsToSimpleOptimizationResultsMap(final List<OptimizationResult<Object>> results, final Map<String, Long> idMap) {
		final Map<String, List<SimpleOptimizationResult>> simpleResults = new HashMap<>(results.size());
		for(final OptimizationResult<?> result : results) {
			if(result != null) {
				List<SimpleOptimizationResult> resultFileList = simpleResults.get(result.getOriginalFile().getName());
				if(resultFileList == null) {
					resultFileList = new ArrayList<>(2);
					simpleResults.put(result.getOriginalFile().getName(), resultFileList);
				}
				resultFileList.add(new SimpleOptimizationResult(result.getOptimizedFileSize(), result.getOriginalFileSize() - result.getOptimizedFileSize(), idMap.get(result.getOriginalFile().getName()).toString(), result.getOptimizedFile().getName()));
			}
		}
		return simpleResults;
	}
	
	private File getFile(final long id, final String fileName) throws OptimizedFileNotFoundException {
		final File file = new File(new StringBuilder(imageOptimizationService.getFinalResultsDirectory()).append(tmpDirPath).append('/').append(id).append('/').append(fileName).toString());
		
		if(!file.exists()) {
			throw new OptimizedFileNotFoundException(fileName + " does not exist");
		}
		return file;
	}
	
	public String getRootImageDirectory() {
		return imageOptimizationService.getFinalResultsDirectory() + tmpDirPath;
	}

	@RequestMapping(value="get/{id}/{fileName:.*}", method=RequestMethod.GET)
	@ResponseBody
	protected FileSystemResource download(@PathVariable final long id, @PathVariable final String fileName, final HttpServletResponse response) throws Exception {
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		return new FileSystemResource(getFile(id, fileName));
	}
	
	/**
	 * The exception that occurs when the user is trying to download an 
	 * optimized image that does not exist. This is possible if the user uses an
	 * incorrect URL or if the images is older than 2 hours (because it has been
	 * deleted by the system).
	 * 
	 * @author eperret (Eric Perret)
	 */
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public class OptimizedFileNotFoundException extends FileNotFoundException {

		/**
		 * Constructor.
		 * 
		 * @see FileNotFoundException#FileNotFoundException(String)
		 */
		public OptimizedFileNotFoundException(final String message) {
			super(message);
		}
		
		private static final long serialVersionUID = 4354806950566003934L;
	    
	}
}