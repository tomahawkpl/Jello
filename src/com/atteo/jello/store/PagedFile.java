package com.atteo.jello.store;

import java.io.File;
import java.io.IOException;


public interface PagedFile {
	void open() throws IOException;
	void close() throws IOException;
	long getFileLength();
	long addPages(long count) throws IOException;
	void removePages(long count) throws IOException;
	void readPage(long id, byte[] data);
	void writePage(long id, byte[] data);
	long getPageCount();
	void syncPages(long startPage, long count);
	void syncAll();
	boolean isReadOnly();
	
	public interface Factory {
		PagedFile create(File file, boolean readOnly);
	}

}
