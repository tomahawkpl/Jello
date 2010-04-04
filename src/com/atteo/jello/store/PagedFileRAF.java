package com.atteo.jello.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.atteo.jello.Jello;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class PagedFileRAF implements PagedFile {
	final File file;

	private boolean readOnly;
	private final int pageSize;
	private int pages;
	private RandomAccessFile raf;

	@Inject
	PagedFileRAF(@Named("pageSize") final short pageSize,
			@Named("fullpath") final String fullpath) {
		this.pageSize = pageSize;
		
		file = new File(fullpath);
	}

	public int addPages(final int count) {
		pages += count;
		try {
			raf.setLength(pages * pageSize);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return pages - 1;
	}

	public void close() {
		try {
			raf.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public long getFileLength() {
		long len = 0;
		try {
			len = raf.length();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return len;
	}

	public int getPageCount() {
		return pages;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public int open() {
		if (!exists())
			return Jello.OPEN_FAILED;
		
		if (!file.canRead())
			return Jello.OPEN_FAILED;

		readOnly = !file.canWrite();

		String mode;
		if (readOnly)
			mode = "r";
		else
			mode = "rw";

		try {
			raf = new RandomAccessFile(file, mode);
		} catch (final FileNotFoundException e1) {
			e1.printStackTrace();
			return Jello.OPEN_FAILED;

		}

		try {
			pages = (int) (raf.length() / pageSize);
		} catch (final IOException e) {
			e.printStackTrace();
			return Jello.OPEN_FAILED;

		}

		if (readOnly)
			return Jello.OPEN_READONLY;
		else
			return Jello.OPEN_SUCCESS;
	}

	public void readPage(final Page page) {
		try {
			raf.seek(page.getId() * pageSize);
			raf.readFully(page.getData());
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

	public void removePages(final int count) {
		pages -= count;
		if (pages < 0)
			pages = 0;
		try {
			raf.setLength(pages * pageSize);
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

	public void syncAll() {

	}

	public void syncPages(final int startPage, final int count) {

	}

	public void writePage(final Page page) {
		try {
			raf.seek(page.getId() * pageSize);
			raf.write(page.getData());
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public boolean create() {
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	public void remove() {
		file.delete();
	}

	public boolean exists() {
		return file.exists();
	}

}
