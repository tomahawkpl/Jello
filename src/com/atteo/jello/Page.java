package com.atteo.jello;

public class Page {
	byte data[] = null;
	boolean dirty = false;
	int accessCount = 0;

	Page() {

	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
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
