package com.atteo.jello.store;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Page {
	public Page nextInPool = null; // public for performance reasons
	private int accessCount = 0;
	private byte data[] = null;
	private boolean dirty = false;

	@Inject
	Page(@Named("pageSize") final int pageSize) {
		data = new byte[pageSize];
	}

	public void decreaseAccessCount() {
		accessCount++;
	}

	public int getAccessCount() {
		return accessCount;
	}

	public byte[] getData() {
		return data;
	}

	public void increaseAccessCount() {
		accessCount++;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}

}
