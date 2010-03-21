package com.atteo.jello.store;

import com.google.inject.Inject;

public class KlassManager {
	private PagedFile pagedFile;
	private ListPage klasses;
	
	@Inject
	private KlassManager(PagedFile pagedFile) {
		this.pagedFile = pagedFile;
	}
	
	public void create() {
		
	}
	
	public boolean load() {
		return true;
	}
}
