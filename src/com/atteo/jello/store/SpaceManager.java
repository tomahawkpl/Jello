package com.atteo.jello.store;

public interface SpaceManager {
	boolean load();
	void create();
	void update(); 
	
	void setBlockUsed(long id, int block, boolean used);
	boolean isBlockUsed(long id, int block);
	
	void setPageUsed(long id, boolean used);
	boolean isPageUsed(long id);
}