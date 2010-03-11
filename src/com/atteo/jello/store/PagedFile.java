package com.atteo.jello.store;

import java.io.IOException;


public interface PagedFile {
	int open() throws IOException;
	void close() throws IOException;
	long getFileLength();
	int addPages(int count) throws IOException;
	void removePages(int count) throws IOException;
	void readPage(Page page);
	void writePage(Page page);
	int getPageCount();
	void syncPages(int startPageId, int count);
	void syncAll();
	boolean isReadOnly();
}