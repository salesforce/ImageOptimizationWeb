package com.salesforce.perfeng.imageoptimization.web.domain;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/**
 * DTO for files being uploaded.
 * 
 * @author eperret (Eric Perret)
 */
public class FileUploadForm {
	private List<MultipartFile> files;

	/**
	 * @return the files.
	 */
	public final List<MultipartFile> getFiles() {
		return files;
	}

	/**
	 * @param files the files to set.
	 */
	public final void setFiles(List<MultipartFile> files) {
		this.files = files;
	}
}