package com.atteo.jello.space;

import com.atteo.jello.Record;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppendOnly implements SpaceManagerPolicy {
	static {
		System.loadLibrary("AppendOnly");
	}
	
	@Inject
	public AppendOnly(AppendOnlyCache appendOnlyCache) {
		init(appendOnlyCache);
	}
	
	private native void init(AppendOnlyCache appendOnlyCache);
	public native int acquirePage();
	public native Record acquireRecord(int length);
	public native void reacquireRecord(Record record, int length);
	public native void releasePage(int id);
	public native void releaseRecord(Record record);
}
