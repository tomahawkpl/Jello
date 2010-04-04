package com.atteo.jello.space;

public interface AppendOnlyCache {
	public static int NO_PAGE = -1;

	public int getBestId(short freeSpace);

	public short getFreeSpace(int id);

	public boolean isEmpty();

	public void update(int id, short freeSpace);
}