package com.atteo.jello.store;

import java.io.IOException;

import com.atteo.jello.guice.BufferedPagedFileBase;
import com.google.inject.name.Named;

class BufferedPagedFile implements PagedFile {
	private final int bufferSize;
	private final PagedFile pagedFile;

	BufferedPagedFile(@BufferedPagedFileBase final PagedFile pagedFile,
			@Named("bufferSize") final int bufferSize) {
		this.pagedFile = pagedFile;
		this.bufferSize = bufferSize;

	}

	
	public int addPage() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public void getPage(final int id, final Page page) throws IOException {
		pagedFile.getPage(id, page);
	}

	
	public int getPageCount() {
		return pagedFile.getPageCount();
	}

	
	public void removePage() {
		// TODO Auto-generated method stub

	}

	
	public void writePage(final int id, final Page page) throws IOException {
		// TODO Auto-generated method stub
	}

}
