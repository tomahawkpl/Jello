package com.atteo.jello.space;

import com.atteo.jello.Record;

public interface SpaceManagerPolicy {
	public static int ACQUIRE_FAILED = -1;

	public int acquirePage();

	public Record acquireRecord(int length);

	public void reacquireRecord(Record record, int length);

	public void releasePage(int id);

	public void releaseRecord(Record record);
}