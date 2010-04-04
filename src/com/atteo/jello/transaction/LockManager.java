package com.atteo.jello.transaction;

import com.atteo.jello.Record;

public interface LockManager {
	public static final int LOCK_DENIED = -1;

	public int acquirePageLock(int id, boolean exclusive);

	public int acquireRecordLock(Record record, boolean exclusive);

	public void releaseLock(int lockId);
}
