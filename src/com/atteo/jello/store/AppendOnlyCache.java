package com.atteo.jello.store;

public interface AppendOnlyCache {
	public boolean isEmpty();
	public long getBestId(int freeSpace);
	public int getFreeSpace(long id);
	public void update(long id, int freeSpace);
}