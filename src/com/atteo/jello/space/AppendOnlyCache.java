package com.atteo.jello.space;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class AppendOnlyCache {

	private int cacheSize;
	
	@Inject
	private AppendOnlyCache(@Named("appendOnlyCacheSize") int cacheSize) {
		this.cacheSize = cacheSize;
	}
	
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void update(long pageId, int freeBytes) {
		
	}
	
	public long getBest() {
		return -1;
	}
	
	public int getFreeSpace(long id) {
		return -1;
	}

}
