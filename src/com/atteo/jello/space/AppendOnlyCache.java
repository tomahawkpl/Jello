package com.atteo.jello.space;

public interface AppendOnlyCache {
	public static int NO_PAGE = -1;
	
	public boolean isEmpty();
	public int getBestId(short freeSpace);
	public short getFreeSpace(int id);
	public void update(int id, short freeSpace);
}