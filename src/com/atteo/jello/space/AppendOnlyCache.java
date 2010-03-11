package com.atteo.jello.space;

public interface AppendOnlyCache {
	public boolean isEmpty();
	public int getBestId(short freeSpace);
	public short getFreeSpace(int id);
	public void update(int id, short freeSpace);
}