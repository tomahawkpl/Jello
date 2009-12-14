package com.atteo.jello.store;

import com.google.inject.Inject;

public class KlassManager {
	private PagedFile pagedFile;
	
	@Inject
	private KlassManager(PagedFile pagedFile) {
		this.pagedFile = pagedFile;
	}
}
