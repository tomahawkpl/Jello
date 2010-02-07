package com.atteo.jello.store;

import java.nio.ByteBuffer;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Page {
	public Page nextInPool = null; // public for performance reasons
	protected int accessCount = 0;
	protected long id;
	protected byte data[] = null;
	protected boolean dirty = false;
	protected ByteBuffer byteBuffer;
	
	@Inject
	Page(@Named("pageSize") final int pageSize) {
		data = new byte[pageSize];
		byteBuffer = ByteBuffer.wrap(data);
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

	public int getCapacity() {
		return data.length;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
}
