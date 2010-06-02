package com.atteo.jello.space;

import com.atteo.jello.Record;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class NextFit implements SpaceManagerPolicy {
	static {
		System.loadLibrary("NextFit");
	}
	
	private SpaceManager spaceManager;
	
	@Inject
	public NextFit(NextFitHistogram nextFitHistogram, PagedFile pagedFile,
			SpaceManager spaceManager, @Named("pageSize") short pageSize,
			@Named("blockSize") short blockSize,
			@Named("maxRecordSize") int maxRecordSize) {
		this.spaceManager = spaceManager;
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

	public void create() {
		spaceManager.create();
	}

	public boolean load() {
		return spaceManager.load();
	}
	
	public void commit() {
		spaceManager.commit();
	}
}
