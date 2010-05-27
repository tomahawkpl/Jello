package com.atteo.jello.store;


public interface PagedFile {
	public static int PAGE_ADD_FAILED = -1;

	int addPages(int count);

	void close();

	long getFileLength();

	int getPageCount();

	boolean isReadOnly();

	int open();

	void readPage(Page page);

	void removePages(int count);

	void syncAll();

	void syncPages(int startPageId, int count);

	void writePage(Page page);

	boolean exists();
	
	boolean create();

	void remove();
}