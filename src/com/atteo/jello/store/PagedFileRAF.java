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
	private long pages;
	private RandomAccessFile raf;

	@Inject
	PagedFileRAF(@Named("pageSize") int pageSize, @Assisted File file, @Assisted boolean readOnly) {
		this.file = file;
		this.pageSize = pageSize;
		this.readOnly = readOnly;
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

	public void open() throws IOException {
		String mode;
		if (readOnly)
			mode = "r";
		else
			mode = "rw";
		
		raf = new RandomAccessFile(file,mode);
		
		pages = (int) (raf.length() / pageSize);
		
	}

	public void readPage(long id, byte[] data) {
		try {
			raf.seek(id * pageSize);
			raf.readFully(data);
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

	public void writePage(long id, byte[] data) {
		try {
			raf.seek(id * pageSize);
			raf.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
