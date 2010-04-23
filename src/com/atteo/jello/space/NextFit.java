package com.atteo.jello.space;

import com.atteo.jello.Record;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class NextFit implements SpaceManagerPolicy {
	static {
		System.loadLibrary("NextFit");
	}

	@Inject
	public NextFit(NextFitHistogram nextFitHistogram, PagedFile pagedFile,
			SpaceManager spaceManager, @Named("pageSize") short pageSize,
			@Named("blockSize") short blockSize,
			@Named("maxRecordSize") int maxRecordSize) {
		init(nextFitHistogram, pagedFile, spaceManager, pageSize, blockSize, maxRecordSize);
	}

	private native void init(NextFitHistogram nextFitHistogram,
			PagedFile pagedFile, SpaceManager spaceManager, short pageSize,
			short blockSize, int maxRecordSize);

	public native int acquirePage();

	public native boolean acquireRecord(Record record, int length);

	public native boolean reacquireRecord(Record record, int length);

	public native void releasePage(int id);

	public native void releaseRecord(Record record);

	public native void create();

	public native boolean load();
}
