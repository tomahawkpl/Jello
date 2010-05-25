package com.atteo.jello.space;


import com.atteo.jello.Record;
import com.atteo.jello.store.ListPage;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class SpaceManagerNative implements SpaceManager {
	// TODO: currently keeps all freeSpaceInfo pages in memory
	static {
		System.loadLibrary("SpaceManagerNative");
	}

	@Inject
	public SpaceManagerNative(
			PagedFile pagedFile,
			ListPage listPage,
			@Named("freeSpaceInfoSize") final short freeSpaceInfoSize,
			@Named("freeSpaceInfosPerPage") final short freeSpaceInfosPerPage,
			@Named("freeSpaceInfoPageCapacity") final short freeSpaceInfoPageCapacity,
			@Named("blockSize") final short blockSize,
			@Named("freeSpaceInfoPageId") int freeSpaceInfoPageId) {

		init(pagedFile, listPage, freeSpaceInfosPerPage, freeSpaceInfoSize,
				freeSpaceInfoPageCapacity, freeSpaceInfoPageId,
				blockSize);

	}

	public native void create();

	public native short freeSpaceOnPage(int id);

	public native void init(PagedFile pagedFile, ListPage listPage,
			short freeSpaceInfosPerPage, short freeSpaceInfoSize,
			short freeSpaceInfoPageCapacity, int freeSpaceInfoPageId,
			short blockSize);

	public native boolean isBlockUsed(int id, short block);

	public native boolean isPageUsed(int id);

	public native boolean load();

	public native void setBlockUsed(int id, short block, boolean used);

	public native void setPageUsed(int id, boolean used);

	public native long totalFreeSpace();

	public native void update();

	public native void setRecordUsed(Record record, boolean used);

	public native void commit();
}
