package com.atteo.jello.space;

public interface SpaceManager {
	void create();

	short freeSpaceOnPage(int id);

	boolean isBlockUsed(int id, short block);

	boolean isPageUsed(int id);

	boolean load();

	void setAreasUsed(int id, byte[] areas, boolean used);
	
	void setBlockUsed(int id, short block, boolean used);

	void setPageUsed(int id, boolean used);

	long totalFreeSpace();

	void update();
}