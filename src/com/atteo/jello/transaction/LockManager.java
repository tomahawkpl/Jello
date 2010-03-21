package com.atteo.jello.transaction;

import com.atteo.jello.space.Record;

public interface LockManager {
	public static final int LOCK_DENIED = -1;
	
	public int acquirePageLock(int id);
	public int acquireRecordLock(Record record);
	public void releaseLock(int lockId);
}
