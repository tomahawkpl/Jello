package com.atteo.jello.store;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.atteo.jello.Jello;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class PagedFileRAF implements PagedFile {
	private String fullpath;
	private boolean readOnly;
	private int pageSize;
	private long pages;
	private RandomAccessFile raf;

	@Inject
	PagedFileRAF(@Named("pageSize") int pageSize, @Named("fullpath") final String fullpath) {
		this.pageSize = pageSize;
		this.fullpath = fullpath;
	}

	public long addPages(long count) {
		pages += count;
		try {
			raf.setLength(pages * pageSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pages - 1;
	}

	public void close() {
		try {
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public long getFileLength() {
		int len = 0;
		try {
			len = (int)raf.length();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return len;
	}

	public long getPageCount() {
		return pages;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public int open() throws IOException {
		File file = new File(fullpath);
		if (!file.exists())
			file.createNewFile();
		
		if (!file.canRead())
			return Jello.OPEN_FAILED;

		readOnly = !file.canWrite();
		
		String mode;
		if (readOnly)
			mode = "r";
		else
			mode = "rw";
		
		raf = new RandomAccessFile(file,mode);
		
		pages = (int) (raf.length() / pageSize);

		if (readOnly)
			return Jello.OPEN_READONLY;
		else
			return Jello.OPEN_SUCCESS;
	}

	public void readPage(Page page) {
		try {
			raf.seek(page.getId() * pageSize);
			raf.readFully(page.getData());
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}

	public void removePages(long count){
		pages -= count;
		if (pages < 0)
			pages = 0;
		try {
			raf.setLength(pages * pageSize);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void syncAll() {

	}

	public void syncPages(long startPage, long count) {

	}

	public void writePage(Page page) {
		try {
			raf.seek(page.getId() * pageSize);
			raf.write(page.getData());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
