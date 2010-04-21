package com.atteo.jello.space;

import com.atteo.jello.Record;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class AppendOnly implements SpaceManagerPolicy {
	static {
		System.loadLibrary("AppendOnly");
	}

	@Inject
	public AppendOnly(AppendOnlyCache appendOnlyCache,
			SpaceManager spaceManager, PagedFile pagedFile,
			@Named("pageSize") short pageSize,
			@Named("blockSize") short blockSize,
			@Named("maxRecordSize") int maxRecordSize) {
		init(appendOnlyCache, spaceManager, pagedFile, pageSize, blockSize,
				maxRecordSize);

	}

	private native void init(AppendOnlyCache appendOnlyCache,
			SpaceManager spaceManager, PagedFile pagedFile, short pageSize,
			short blockSize, int maxRecordSize);

	public native int acquirePage();

	public native boolean acquireRecord(Record record, int length);

	public native boolean reacquireRecord(Record record, int length);

	public native void releasePage(int id);

	public native void releaseRecord(Record record);
}
