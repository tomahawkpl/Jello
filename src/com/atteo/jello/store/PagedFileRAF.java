package com.atteo.jello.store;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

public class PagedFileRAF implements PagedFile {
	private File file;
	private boolean readOnly;
	private int pageSize;
	private int pages;
	private RandomAccessFile raf;

	@Inject
	PagedFileRAF(@Named("pageSize") int pageSize, @Assisted File file, @Assisted boolean readOnly) {
		this.file = file;
		this.pageSize = pageSize;
		this.readOnly = readOnly;
	}

	public int addPages(int count) throws IOException {
		pages += count;
		raf.setLength(pages * pageSize);
		return pages - 1;
	}

	public void close() throws IOException {
		raf.close();
	}

	public int getFileLength() {
		int len = 0;
		try {
			len = (int)raf.length();
		} catch (IOException e) {
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

	public void open() throws IOException {
		String mode;
		if (readOnly)
			mode = "r";
		else
			mode = "rw";
		
		raf = new RandomAccessFile(file,mode);
		
		pages = (int) (raf.length() / pageSize);
		
	}

	public void readPage(int id, byte[] data) throws IOException {
		raf.seek(id * pageSize);
		raf.readFully(data);

	}

	public void removePages(int count) throws IOException {
		pages -= count;
		if (pages < 0)
			pages = 0;
		raf.setLength(pages * pageSize);

	}

	public void syncAll() {

	}

	public void syncPages(int startPage, int count) {

	}

	public void writePage(int id, byte[] data) throws IOException {
		raf.seek(id * pageSize);
		raf.write(data);

	}

}
