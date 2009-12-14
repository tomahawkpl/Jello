package com.atteo.jello.store;

import java.io.File;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;

@Singleton
public class DatabaseFile {
	private static final int MIN_PAGES = 3;
	private static final int PAGE_HEADER = 0;
	private static final int PAGE_FREE_LIST = 1;
	private static final int PAGE_KLASS_LIST = 2;
	
	private PagedFile pagedFile;
	private HeaderPage headerPage;
	private SpaceManager spaceManager;
	private KlassManager klassManager;
	private Injector injector;
	
	private boolean valid;
	
	@Inject
	private DatabaseFile(Injector injector, PagedFile pagedFile) throws IOException {
		this.injector = injector;
		this.pagedFile = pagedFile;
		pagedFile.open();
		
		valid = false;

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
		
		headerPage = injector.getInstance(HeaderPage.class);
		freeSpaceMap = injector.getInstance(ListPage.class);
		listOfClasses = injector.getInstance(ListPage.class);
		
		if (pagedFile.getPageCount() < MIN_PAGES)
			return;
		
		pagedFile.readPage(0, headerPage.getData());
		if (!headerPage.load())
			return;
		pagedFile.readPage(1, freeSpaceMap.getData());
		pagedFile.readPage(2, listOfClasses.getData());
		
		valid = true;
		
	}
	
	private void createStructure() {
		try {
			pagedFile.addPages(3);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		headerPage = injector.getInstance(HeaderPage.class);
		pagedFile.writePage(0, headerPage.getData());
		
		freeSpaceMap = injector.getInstance(ListPage.class);
		freeSpaceMap.setNext(0);
		listOfClasses = injector.getInstance(ListPage.class);
		listOfClasses.setNext(0);
		
		
		

		
		pagedFile.writePage(1, freeSpaceMap.getData());
		pagedFile.writePage(2, listOfClasses.getData());
		
		valid = true;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public interface Factory {
		DatabaseFile create(PagedFile pagedFile);
	}
}