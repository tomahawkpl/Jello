package com.atteo.jello.store;

import java.io.IOException;

import com.atteo.jello.guice.BufferedPagedFileBase;
import com.google.inject.name.Named;

class BufferedPagedFile implements PagedFile {
	private PagedFile pagedFile;
	private int bufferSize;

	BufferedPagedFile(@BufferedPagedFileBase PagedFile pagedFile,
			@Named("bufferSize") int bufferSize) {
		this.pagedFile = pagedFile;
		this.bufferSize = bufferSize;
		
	}

	@Override
	public void getPage(int id, Page page) throws IOException {
		pagedFile.getPage(id, page);
	}

	@Override
	public int getPageCount() {
		return pagedFile.getPageCount();
	}

	@Override
	public int addPage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void removePage() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writePage(int id, Page page) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
