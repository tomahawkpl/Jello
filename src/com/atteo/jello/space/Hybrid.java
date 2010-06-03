package com.atteo.jello.space;

import com.atteo.jello.Record;
import com.atteo.jello.store.ListPage;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class Hybrid implements SpaceManagerPolicy {
	static {
		System.loadLibrary("Hybrid");
	}

	@Inject
	public Hybrid(
			final PagedFile pagedFile,
			final ListPage listPage,
			@Named("pageSize") final short pageSize,
			@Named("blockSize") final short blockSize,
			@Named("maxRecordSize") final int maxRecordSize,
			@Named("freeSpaceInfosPerPage") final short freeSpaceInfosPerPage,
			@Named("freeSpaceInfoSize") final short freeSpaceInfoSize,
			@Named("freeSpaceInfoPageCapacity") final short freeSpaceInfoPageCapacity,
			@Named("freeSpaceInfoPageId") final int pageFreeSpaceInfo,
			@Named("nextFitHistogramClasses") final int histogramClasses) {
		init(pagedFile, listPage, pageSize, blockSize, maxRecordSize,
				freeSpaceInfosPerPage, freeSpaceInfoSize,
				freeSpaceInfoPageCapacity, pageFreeSpaceInfo, histogramClasses);
	}

	public native int acquirePage();

	public native boolean acquireRecord(Record record, int length);

	public native void commit();

	public native void create();

	public native boolean isPageUsed(int id);

	public native boolean load();

	public native boolean reacquireRecord(Record record, int length);

	public native void releasePage(int id);

	public native void releaseRecord(Record record);

	public native void setPageUsed(int id, boolean used);

	private native void init(PagedFile pagedFile, ListPage listPage,
			short pageSize, short blockSize, int maxRecordSize,
			short freeSpaceInfosPerPage, short freeSpaceInfoSize,
			short freeSpaceInfoPageCapacity, int pageFreeSpaceInfo,
			int histogramClasses);
}
