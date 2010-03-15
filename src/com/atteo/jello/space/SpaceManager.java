package com.atteo.jello.space;

public interface SpaceManager {
	boolean load();
	void create();
	void update(); 
	
	void setBlockUsed(int id, short block, boolean used);
	boolean isBlockUsed(int id, short block);
	
	long totalFreeSpace();
	short freeSpaceOnPage(int id);
	
	void setPageUsed(int id, boolean used);
	boolean isPageUsed(int id);
}