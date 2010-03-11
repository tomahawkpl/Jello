package com.atteo.jello.store;

import java.io.File;
import java.io.IOException;

import com.atteo.jello.Jello;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class PagedFileNative implements PagedFile {
	private String fullpath;
	private boolean readOnly;
	private short pageSize;

	static {
		System.loadLibrary("PagedFileNative");
	}

	@Inject
	PagedFileNative(@Named("pageSize") short pageSize, @Named("fullpath") final String fullpath) {
		this.fullpath = fullpath;
		this.pageSize = pageSize;
	}

	protected void finalize() {
		try {
			close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	synchronized public int open() throws IOException {
		File file = new File(fullpath);
		if (!file.exists())
			file.createNewFile();
		
		if (!file.canRead())
			return Jello.OPEN_FAILED;

		readOnly = !file.canWrite();

		openNative(file.getCanonicalPath(), readOnly, pageSize);
		
		if (readOnly)
			return Jello.OPEN_READONLY;
		else
			return Jello.OPEN_SUCCESS;
	}

	public boolean isReadOnly() {
		return readOnly;
	}
	
	native private int openNative(String fullpath, boolean readOnly,
			short pageSize) throws IOException;
	synchronized native public void close() throws IOException;
	synchronized native public int addPages(int count) throws IOException;
	synchronized native public void removePages(int count) throws IOException;
	native public long getFileLength();
	native public int getPageCount();
	native public void syncPages(int startPage, int count);
	native public void syncAll();
	native public void readPage(Page page);
	synchronized native public void writePage(Page page);

}
