package com.atteo.jello.store;

import java.io.IOException;

import com.atteo.jello.Jello;
import com.atteo.jello.space.SpaceManager;
import com.atteo.jello.space.SpaceManagerNative;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class DatabaseFile {
	public static final long MIN_PAGES = 3;
	public static final long PAGE_HEADER = 0;
	public static final long PAGE_FREE_SPACE_MAP = 1;
	public static final long PAGE_KLASS_LIST = 2;

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
		try {
			return pagedFile.open();
		} catch (IOException e) {
			e.printStackTrace();
			return Jello.OPEN_FAILED;
		}
	}
	
	public void close() {
		try {
			pagedFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		try {
			pagedFile.addPages(3);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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