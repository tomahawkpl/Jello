package com.atteo.jello.transaction;

import com.atteo.jello.Record;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SimpleLockManager implements LockManager {

	static {
		System.loadLibrary("SimpleLockManager");
	}

	@Inject
	private SimpleLockManager() {

	}

	public native int acquirePageLock(int id, boolean exclusive);

	public native int acquireRecordLock(Record record, boolean exclusive);

	public native void init();

	public native void releaseLock(int lockId);
}
