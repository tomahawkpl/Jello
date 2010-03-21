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
	public
	PagedFileNative(@Named("pageSize") short pageSize, @Named("fullpath") final String fullpath) {
		this.fullpath = fullpath;
		this.pageSize = pageSize;
	}

	protected void finalize() {
		close();
	}

	synchronized public int open() {
		File file = new File(fullpath);
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return Jello.OPEN_FAILED;
			}
		
		if (!file.canRead())
			return Jello.OPEN_FAILED;

		readOnly = !file.canWrite();

		try {
			openNative(file.getCanonicalPath(), readOnly, pageSize);
		} catch (IOException e) {
			e.printStackTrace();
			return Jello.OPEN_FAILED;
		}
		
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
	synchronized native public void close();
	synchronized native public int addPages(int count);
	synchronized native public void removePages(int count);
	native public long getFileLength();
	native public int getPageCount();
	native public void syncPages(int startPage, int count);
	native public void syncAll();
	native public void readPage(Page page);
	synchronized native public void writePage(Page page);

}
