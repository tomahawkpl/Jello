package com.atteo.jello.space;

import com.atteo.jello.Record;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class NextFit implements SpaceManagerPolicy {
	static {
		System.loadLibrary("NextFit");
	}

	private final SpaceManager spaceManager;

	@Inject
	public NextFit(final NextFitHistogram nextFitHistogram,
			final PagedFile pagedFile, final SpaceManager spaceManager,
			@Named("pageSize") final short pageSize,
			@Named("blockSize") final short blockSize,
			@Named("maxRecordSize") final int maxRecordSize) {
		this.spaceManager = spaceManager;
		init(nextFitHistogram, pagedFile, spaceManager, pageSize, blockSize,
				maxRecordSize);
	}

	public native int acquirePage();

	public native boolean acquireRecord(Record record, int length);

	public void commit() {
		spaceManager.commit();
	}

	public void create() {
		spaceManager.create();
	}

	public boolean isPageUsed(final int id) {
		return spaceManager.isPageUsed(id);
	}

	public boolean load() {
		return spaceManager.load();
	}

	public native boolean reacquireRecord(Record record, int length);

	public native void releasePage(int id);

	public native void releaseRecord(Record record);

	public void setPageUsed(final int id, final boolean used) {
		spaceManager.setPageUsed(id, used);
	}

	private native void init(NextFitHistogram nextFitHistogram,
			PagedFile pagedFile, SpaceManager spaceManager, short pageSize,
			short blockSize, int maxRecordSize);
}
