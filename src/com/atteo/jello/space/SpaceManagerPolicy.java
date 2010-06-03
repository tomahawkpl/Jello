package com.atteo.jello.space;

import com.atteo.jello.Record;

public interface SpaceManagerPolicy {
	public static int ACQUIRE_FAILED = -1;

	public int acquirePage();

	public boolean acquireRecord(Record record, int length);

	public void commit();

	public void create();

	public boolean isPageUsed(int id);

	public boolean load();

	public boolean reacquireRecord(Record record, int length);

	public void releasePage(int id);

	public void releaseRecord(Record record);

	public void setPageUsed(int id, boolean used);
}