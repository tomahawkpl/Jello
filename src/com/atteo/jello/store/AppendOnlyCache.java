package com.atteo.jello.store;

public interface AppendOnlyCache {
	public boolean isEmpty();
	public long getBestId();
	public int getBestFreeSpace();
	public void update(long id, int freeSpace);
}
