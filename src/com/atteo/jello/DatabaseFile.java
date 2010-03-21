package com.atteo.jello;

import java.io.IOException;

import com.atteo.jello.space.SpaceManager;
import com.atteo.jello.space.SpaceManagerNative;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.store.HeaderPage;
import com.atteo.jello.store.KlassManager;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class DatabaseFile {
	public static final int MIN_PAGES = 3;
	public static final int PAGE_HEADER = 0;
	public static final int PAGE_FREE_SPACE_MAP = 1;
	public static final int PAGE_KLASS_LIST = 2;

	private PagedFile pagedFile;
	private HeaderPage headerPage;
	private SpaceManager spaceManager;
	private SpaceManagerPolicy spaceManagerPolicy;

	private KlassManager klassManager;
	private Injector injector;
	
	private boolean valid;

	@Inject
	private DatabaseFile(Injector injector, PagedFile pagedFile) throws IOException {
		this.injector = injector;
		this.pagedFile = pagedFile;
		
		valid = false;
	}
	
	/**
	 * Either loadStructure or createStructure must be called in order for isValid to return true
	 * @return
	 */
	public int open() {
		return pagedFile.open();
	}
	
	public void close() {
		pagedFile.close();
	}
	
	public void loadStructure(boolean create) {
		if (create) {
			createStructure();
			return;
		}
				
		if (pagedFile.getPageCount() < MIN_PAGES)
			return;
		
		headerPage.setId(PAGE_HEADER);
		pagedFile.readPage(headerPage);
		if (!headerPage.load())
			return;
		
		spaceManager = injector.getInstance(SpaceManager.class);
		if (!spaceManager.load())
			return;
		klassManager = injector.getInstance(KlassManager.class);
		if (!klassManager.load())
			return;
		
		spaceManagerPolicy = injector.getInstance(SpaceManagerPolicy.class);
		
		valid = true;
		
	}
	
	private void createStructure() {
		pagedFile.addPages(3);
		
		headerPage = injector.getInstance(HeaderPage.class);
		headerPage.setId(PAGE_HEADER);
		pagedFile.writePage(headerPage);

		spaceManager = injector.getInstance(SpaceManagerNative.class);
		spaceManager.create();
		
		klassManager = injector.getInstance(KlassManager.class);
		klassManager.create();
		
		valid = true;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public PagedFile getPagedFile() {
		return pagedFile;
	}
	
	public SpaceManager getSpaceManager() {
		return spaceManager;
	}
}