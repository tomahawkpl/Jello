package com.atteo.jello.space;

public interface SpaceManager {
	boolean load();
	void create();
	void update(); 
	
	void setBlockUsed(long id, int block, boolean used);
	boolean isBlockUsed(long id, int block);
	
	void setPageUsed(long id, boolean used);
	boolean isPageUsed(long id);
}