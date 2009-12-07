package com.atteo.jello.store;

import java.io.IOException;


public interface PagedFile {
	void open() throws IOException;
	void close() throws IOException;
	int getFileLength();
	int addPages(int count) throws IOException;
	void removePages(int count) throws IOException;
	void readPage(int id, byte[] data) throws IOException;
	void writePage(int id, byte[] data) throws IOException;
	int getPageCount();
	void syncPages(int startPage, int count);
	void syncAll();
	boolean isReadOnly();
}
