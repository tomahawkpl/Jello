package com.atteo.jello.space;

import com.atteo.jello.Record;

public class NextFit implements SpaceManagerPolicy {
	static {
		System.loadLibrary("NextFit");
	}
	
	public native int acquirePage();
	public native void acquireRecord(Record record, int length);
	public native void reacquireRecord(Record record, int length);
	public native void releasePage(int id);
	public native void releaseRecord(Record record);
}
