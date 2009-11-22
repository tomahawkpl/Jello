package com.atteo.jello.store;

import com.google.inject.Inject;
import com.google.inject.name.Named;


public class Page {
	private byte data[] = null;
	private boolean dirty = false;
	private int accessCount = 0;
	public Page nextInPool = null;
	
	@Inject
	Page(@Named("pageSize") int pageSize) {
		data = new byte[pageSize];
	}

	public byte[] getData() {
		return data;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public int getAccessCount() {
		return accessCount;
	}

	public void increaseAccessCount() {
		this.accessCount++;
	}
	
	public void decreaseAccessCount() {
		this.accessCount++;
	}


}
