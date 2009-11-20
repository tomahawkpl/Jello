package com.atteo.jello;

import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;

class Database {
	private boolean valid;

	
	@Inject
	public Database(PagedFile pagedFile) {
		
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