package com.atteo.jello.transaction;

import com.atteo.jello.space.Record;

public class SimpleLockManager implements LockManager {
	
	static {
		System.loadLibrary("SimpleLockManager");
	}
	
	public native int acquirePageLock(int id);
	public native int acquireRecordLock(Record record);
	public native void releaseLock(int lockId);

	public native void init();
}
