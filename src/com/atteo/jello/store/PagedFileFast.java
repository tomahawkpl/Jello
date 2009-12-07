package com.atteo.jello.store;

import java.io.File;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

public class PagedFileFast implements PagedFile {
	private File file;
	private boolean readOnly;
	private int pageSize;

	static {
		System.loadLibrary("PagedFileFast");
	}

	@Inject
	PagedFileFast(@Named("pageSize") int pageSize, @Assisted final File file,
			@Assisted boolean readOnly) {
		this.file = file;
		this.readOnly = readOnly;
		this.pageSize = pageSize;
	}

	protected void finalize() {
		close();
	}

	synchronized public void open() throws IOException {
		if (file == null || !file.exists())
			throw new IllegalArgumentException(
					"File argument is null or does not exist");

		if (!file.canRead())
			throw new IOException("File is not readable");

		if (!readOnly && !file.canWrite())
			readOnly = true;

		openNative(file.getCanonicalPath(), readOnly, pageSize);

	}

	native private int openNative(String fullpath, boolean readOnly,
			int pageSize) throws IOException;
	synchronized native public void close();
	synchronized native public int addPages(int count) throws IOException;
	synchronized native public void removePages(int count) throws IOException;
	native public int getFileLength();
	native public int getPageCount();
	native public boolean isReadOnly();
	native public void syncPages(int startPage, int count);
	native public void syncAll();
	native public void readPage(final int id, final byte[] data);
	synchronized native public void writePage(final int id, final byte[] data);

}
