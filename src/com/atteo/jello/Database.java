package com.atteo.jello;

import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;

class Database {
	private boolean valid;

	@Inject
	public Database(final PagedFile pagedFile) {

		readHeader();
		readTableOfContents();
	}

	public boolean isValid() {
		return valid;
	}

	private void readHeader() {
		// TODO Auto-generated method stub

	}

	private void readTableOfContents() {
		// TODO Auto-generated method stub

	}

}