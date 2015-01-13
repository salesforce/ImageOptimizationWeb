package com.salesforce.perfeng.imageoptimization.web.domain;

/**
 * Meta data about the optimized file.
 * 
 * @author eperret (Eric Perret)
 */
public class FileMeta {
		 
    private String fileName;
    private String fileSize;
    private String fileType;
 
    private byte[] bytes;

	/**
	 * @return the fileName
	 */
	public final String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public final void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the fileSize
	 */
	public final String getFileSize() {
		return fileSize;
	}

	/**
	 * @param fileSize the fileSize to set
	 */
	public final void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * @return the fileType
	 */
	public final String getFileType() {
		return fileType;
	}

	/**
	 * @param fileType the fileType to set
	 */
	public final void setFileType(String fileType) {
		this.fileType = fileType;
	}

	/**
	 * @return the bytes
	 */
	public final byte[] getBytes() {
		return bytes;
	}

	/**
	 * @param bytes the bytes to set
	 */
	public final void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
}