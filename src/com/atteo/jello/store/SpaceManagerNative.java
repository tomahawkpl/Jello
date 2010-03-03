package com.atteo.jello.store;

import java.util.ArrayList;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class SpaceManagerNative implements SpaceManager {
	static {
		System.loadLibrary("SpaceManagerNative");
	}
	
	private final int freeSpaceInfoSize;
	private final int pageSize;
	@SuppressWarnings("unused")
	private final int freeSpaceMapPageCapacity;
	private final int blocksPerPage;
	private final int freeSpaceInfosPerPage;

	@SuppressWarnings("unused")
	private PagedFile pagedFile;
	
	@Inject
	public SpaceManagerNative(PagedFile pagedFile,
			@Named("freeSpaceInfoSize") int freeSpaceInfoSize,
			@Named("pageSize") int pageSize, @Named("blocksPerPage") int blocksPerPage,
			@Named("freeSpaceInfosPerPage") int freeSpaceInfosPerPage,
			ListPage listPage) {
		this.pagedFile = pagedFile;

		this.blocksPerPage = blocksPerPage;
		this.freeSpaceInfosPerPage = freeSpaceInfosPerPage;
		
		this.freeSpaceInfoSize = freeSpaceInfoSize;
		this.freeSpaceMapPageCapacity = listPage.getCapacity();
		this.pageSize = pageSize;
		
		init(this, listPage);
	}
	
	public native void init(SpaceManagerNative spaceManager, ListPage listPage);

	public native void create();	
	public native boolean load();
	public native void update();
	
	public native boolean isPageUsed(long id);
	public native void setPageUsed(long id, boolean used);
	
	public native boolean isBlockUsed(long id, int block);
	public native void setBlockUsed(long id, int block, boolean used);
}
