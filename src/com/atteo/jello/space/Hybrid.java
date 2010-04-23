package com.atteo.jello.space;

import com.atteo.jello.Record;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Hybrid implements SpaceManagerPolicy {
	static {
		System.loadLibrary("Hybrid");
	}
	
	@Inject
	public Hybrid() {
		init();
	}
	
	private native void init();
	public native int acquirePage();
	public native boolean acquireRecord(Record record, int length);
	public native boolean reacquireRecord(Record record, int length);
	public native void releasePage(int id);
	public native void releaseRecord(Record record);
	public native void create();
	public native boolean load();
}
