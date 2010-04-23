package com.atteo.jello.space;

import com.atteo.jello.Record;

public interface SpaceManagerPolicy {
	public static int ACQUIRE_FAILED = -1;

	public void create();
	public boolean load();
	
	public int acquirePage();

	public boolean acquireRecord(Record record, int length);

	public boolean reacquireRecord(Record record, int length);

	public void releasePage(int id);

	public void releaseRecord(Record record);
}