package com.atteo.jello.store;



public interface PagedFile {
	public static int PAGE_ADD_FAILED = -1;
	int open();
	void close();
	long getFileLength();
	int addPages(int count);
	void removePages(int count);
	void readPage(Page page);
	void writePage(Page page);
	int getPageCount();
	void syncPages(int startPageId, int count);
	void syncAll();
	boolean isReadOnly();
}