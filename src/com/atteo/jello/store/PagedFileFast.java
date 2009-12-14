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
		try {
			close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	synchronized public void open() throws IOException {
		if (file == null || !file.exists())
			throw new IllegalArgumentException(
					"File argument is null or does not exist");

		if (!file.canRead())
			throw new IOException("File is not readable");

		if (!readOnly && !file.canWrite())
			throw new IOException("Tried to open a read only file in rw mode");

		openNative(file.getCanonicalPath(), readOnly, pageSize);

	}

	native private int openNative(String fullpath, boolean readOnly,
			int pageSize) throws IOException;
	synchronized native public void close() throws IOException;
	synchronized native public long addPages(long count) throws IOException;
	synchronized native public void removePages(long count) throws IOException;
	native public long getFileLength();
	native public long getPageCount();
	native public boolean isReadOnly();
	native public void syncPages(long startPage, long count);
	native public void syncAll();
	native public void readPage(final long id, final byte[] data);
	synchronized native public void writePage(final long id, final byte[] data);

}
