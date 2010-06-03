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

	synchronized native public int addPages(int count);

	synchronized native public void close();

	public boolean create() {
		file.getParentFile().mkdirs();
		if (!file.getParentFile().exists())
			return false;
		try {
			file.createNewFile();
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean exists() {
		return file.exists();
	}

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

	public void remove() {
		file.delete();
	}

	synchronized native public void removePages(int count);

	native public void syncAll();

	native public void syncPages(int startPage, int count);

	synchronized native public void writePage(Page page);

	private native void init();

	native private int openNative(String fullpath, boolean readOnly,
			short pageSize) throws IOException;

	@Override
	protected void finalize() {
		close();
	}

}
