package com.atteo.jello.space;

import com.atteo.jello.store.DatabaseFile;
import com.atteo.jello.store.ListPage;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class SpaceManagerNative implements SpaceManager {
	static {
		System.loadLibrary("SpaceManagerNative");
	}
	
	@Inject
	public SpaceManagerNative(PagedFile pagedFile,
			@Named("freeSpaceInfoSize") int freeSpaceInfoSize,
			@Named("freeSpaceInfosPerPage") int freeSpaceInfosPerPage,
			@Named("freeSpaceInfoPageCapacity") int freeSpaceInfoPageCapacity,
			ListPage listPage) {
		
		init(pagedFile, listPage, freeSpaceInfosPerPage, freeSpaceInfoSize, freeSpaceInfoPageCapacity, DatabaseFile.PAGE_FREE_SPACE_MAP);
	}
	
	public native void init(PagedFile pagedFile, ListPage listPage,
			int freeSpaceInfosPerPage, int freeSpaceInfoSize, int freeSpaceInfoPageCapacity, 
			long pageFreeSpaceInfo);

	public native void create();	
	public native boolean load();
	public native void update();
	
	public native boolean isPageUsed(long id);
	public native void setPageUsed(long id, boolean used);
	
	public native boolean isBlockUsed(long id, int block);
	public native void setBlockUsed(long id, int block, boolean used);
}
