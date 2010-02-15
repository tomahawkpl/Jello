package com.atteo.jello.store;

import java.io.IOException;


public interface PagedFile {
	int open() throws IOException;
	void close() throws IOException;
	long getFileLength();
	long addPages(long count) throws IOException;
	void removePages(long count) throws IOException;
	void readPage(Page page);
	void writePage(Page page);
	long getPageCount();
	void syncPages(long startPageId, long count);
	void syncAll();
	boolean isReadOnly();
}