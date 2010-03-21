package com.atteo.jello.space;

public interface SpaceManagerPolicy {
	public static int ACQUIRE_FAILED = -1;
	
	public Record acquireRecordSpace(int length);
	public Record reacquireRecordSpace(Record record, int length);
	public void releaseRecordSpace(Record record);
	
	public int acquirePage();
	public void releasePage(int id);
}