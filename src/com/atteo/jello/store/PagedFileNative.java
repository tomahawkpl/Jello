package com.atteo.jello.store;

import java.io.File;
import java.io.IOException;

import com.atteo.jello.Jello;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class PagedFileNative implements PagedFile {
	private boolean readOnly;
	private final short pageSize;
	private final File file;
	
	static {
		System.loadLibrary("PagedFileNative");
	}

	@Inject
	public PagedFileNative(@Named("pageSize") final short pageSize,
			@Named("fullpath") final String fullpath) {
		this.pageSize = pageSize;
		
		file = new File(fullpath);
		
		init();
	}

	private native void init();
	synchronized native public int addPages(int count);

	synchronized native public void close();

	native public long getFileLength();

	native public int getPageCount();

	public boolean isReadOnly() {
		return readOnly;
	}

	synchronized public int open() {
		if (!file.exists())
			return Jello.OPEN_FAILED;

		if (!file.canRead())
			return Jello.OPEN_FAILED;

		readOnly = !file.canWrite();

		try {
			openNative(file.getCanonicalPath(), readOnly, pageSize);
		} catch (final IOException e) {
			e.printStackTrace();
			return Jello.OPEN_FAILED;
		}

		if (readOnly)
			return Jello.OPEN_READONLY;
		else
			return Jello.OPEN_SUCCESS;
	}

	native public void readPage(Page page);

	synchronized native public void removePages(int count);

	native public void syncAll();

	native public void syncPages(int startPage, int count);

	synchronized native public void writePage(Page page);

	native private int openNative(String fullpath, boolean readOnly,
			short pageSize) throws IOException;

	@Override
	protected void finalize() {
		close();
	}

	public boolean create() {
		file.getParentFile().mkdirs();
		if (!file.getParentFile().exists())
			return false;
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean exists() {
		return file.exists();
	}

	public void remove() {
		file.delete();
	}

}
