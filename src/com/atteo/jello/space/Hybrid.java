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
			PagedFile pagedFile,
			ListPage listPage,
			@Named("pageSize") short pageSize,
			@Named("blockSize") short blockSize,
			@Named("maxRecordSize") int maxRecordSize,
			@Named("freeSpaceInfosPerPage") short freeSpaceInfosPerPage,
			@Named("freeSpaceInfoSize") short freeSpaceInfoSize,
			@Named("freeSpaceInfoPageCapacity") short freeSpaceInfoPageCapacity,
			@Named("freeSpaceInfoPageId") int pageFreeSpaceInfo,
			@Named("nextFitHistogramClasses") int histogramClasses) {
		init(pagedFile, listPage, pageSize, blockSize, maxRecordSize,
				freeSpaceInfosPerPage, freeSpaceInfoSize,
				freeSpaceInfoPageCapacity, pageFreeSpaceInfo, histogramClasses);
	}

	private native void init(PagedFile pagedFile, ListPage listPage,
			short pageSize, short blockSize, int maxRecordSize,
			short freeSpaceInfosPerPage, short freeSpaceInfoSize,
			short freeSpaceInfoPageCapacity, int pageFreeSpaceInfo,
			int histogramClasses);

	public native int acquirePage();

	public native boolean acquireRecord(Record record, int length);

	public native boolean reacquireRecord(Record record, int length);

	public native void releasePage(int id);

	public native void releaseRecord(Record record);

	public native void create();

	public native boolean load();
}
