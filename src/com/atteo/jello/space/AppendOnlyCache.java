package com.atteo.jello.space;

public interface AppendOnlyCache {
	public boolean isEmpty();
	public long getBestId(int freeSpace);
	public int getFreeSpace(long id);
	public void update(long id, int freeSpace);
}