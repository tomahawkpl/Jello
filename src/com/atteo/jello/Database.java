package com.atteo.jello;

import java.io.File;
import java.io.IOException;

import com.google.inject.Inject;

class Database {
	private boolean valid;
	private PagedFile pagedFile;
	private TableOfContents tableOfContents;
	
	@Inject
	public Database(PagedFile pagedFile) {
		this.pagedFile = pagedFile;
		
		readHeader();
		readTableOfContents();
	}

	private void readTableOfContents() {
		// TODO Auto-generated method stub
		
	}

	private void readHeader() {
		// TODO Auto-generated method stub
		
	}

	public boolean isValid() {
		return valid;
	}

	

}