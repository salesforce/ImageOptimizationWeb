package com.salesforce.perfeng.imageoptimization.web.domain;

public class SimpleOptimizationResult {
	private final long size;
	private final long savings;
	private final String id;
	private final String file;
	
	public SimpleOptimizationResult(final long size, final long savings, final String id, final String file) {
		this.size = size;
		this.savings = savings;
		this.id = id;
		this.file = file;
	}

	/**
	 * @return the size
	 */
	public final long getSize() {
		return size;
	}

	/**
	 * @return the savings
	 */
	public final long getSavings() {
		return savings;
	}

	/**
	 * @return the id
	 */
	public final String getId() {
		return id;
	}

	/**
	 * @return the file
	 */
	public final String getFile() {
		return file;
	}
}