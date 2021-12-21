package com.salesforce.perfeng.imageoptimization.web.scheduling;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.salesforce.perfeng.imageoptimization.web.controller.ImageFileController;

/**
 * Service used to delete optimized files from the temp storage if they are 
 * older than 2 hours.
 * 
 * @author eperret (Eric Perret)
 */
@Service
public class DeleteOldFiles implements ApplicationContextAware, DisposableBean {
	
	private static final Logger logger = LogManager.getLogger(DeleteOldFiles.class);

	private File finalResultsDirectory;
	
	/**
	 * This method is executed every 1 hour to delete the old files.
	 */
	@Scheduled(fixedDelay = 1200000)
	public void process() {
		
		logger.debug("Starting DeleteOldFiles with {}", finalResultsDirectory);
		if(finalResultsDirectory.exists()) {
			final Calendar twoHoursAgo = Calendar.getInstance();
			twoHoursAgo.add(Calendar.HOUR_OF_DAY, -2);
			Date twoHoursAgoTime = twoHoursAgo.getTime();
			
			final File[] c = finalResultsDirectory.listFiles();
			for(final File directory : c) {
				if(new Date(directory.lastModified()).before(twoHoursAgoTime)) {
					try {
						FileUtils.deleteDirectory(directory);
					} catch (final IOException e) {
						logger.error("Unable to delete directory \"" + directory.getPath() + "\"", e);
					}
				}
			}
		}
		logger.debug("End DeleteOldFiles with {}", finalResultsDirectory);
	}
	
	/**
	 * Used to get and store a reference to the root image directory from 
	 * {@link ImageFileController#getRootImageDirectory()}.
	 * 
	 * @param applicationContext the {@link ApplicationContext} object to be 
	 *                           used by this object
	 * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
	 */
	@Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
		finalResultsDirectory = new File(((ImageFileController)applicationContext.getBean("imageFileController")).getRootImageDirectory());
    }

	/**
	 * When the application is unloaded or restarted this method will be called 
	 * to delete the directory where all of the optimized images live. This is 
	 * done to cleanup and because the references to the images will be lost 
	 * from memory when it is restarted or removed.
	 * 
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		try {
			FileUtils.deleteDirectory(finalResultsDirectory);
		} catch (final Exception e) {
			logger.error("Error deleting " + finalResultsDirectory, e);
		}
		logger.debug("All files deleted from \"{}\".", finalResultsDirectory);
	}
}