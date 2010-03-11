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
			@Named("freeSpaceInfoSize") short freeSpaceInfoSize,
			@Named("freeSpaceInfosPerPage") short freeSpaceInfosPerPage,
			@Named("freeSpaceInfoPageCapacity") short freeSpaceInfoPageCapacity,
			ListPage listPage, @Named("blockSize") short blockSize) {

		init(pagedFile, listPage, freeSpaceInfosPerPage, freeSpaceInfoSize,
				freeSpaceInfoPageCapacity, DatabaseFile.PAGE_FREE_SPACE_MAP,
				blockSize);
	}

	public native void init(PagedFile pagedFile, ListPage listPage,
			short freeSpaceInfosPerPage, short freeSpaceInfoSize,
			short freeSpaceInfoPageCapacity, int pageFreeSpaceInfo, short blockSize);

	public native void create();

	public native boolean load();

	public native void update();

	public native boolean isPageUsed(int id);

	public native void setPageUsed(int id, boolean used);

	public native boolean isBlockUsed(int id, short block);

	public native void setBlockUsed(int id, short block, boolean used);

	public native short freeSpaceOnPage(int id);
}
