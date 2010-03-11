package com.atteo.jello.store;

import java.nio.ByteBuffer;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Page {
	public Page nextInPool = null; // public for performance reasons
	protected int accessCount = 0;
	protected int id;
	protected byte data[] = null;
	protected byte accessibleData[] = null;
	protected boolean dirty = false;
	protected ByteBuffer byteBuffer;
	
	@Inject
	public
	Page(@Named("pageSize") final short pageSize) {
		data = new byte[pageSize];
		byteBuffer = ByteBuffer.wrap(data);
		byteBuffer.position(headerSize());
		accessibleData = byteBuffer.slice().array();
	}

	public void decreaseAccessCount() {
		accessCount++;
	}

	public int getAccessCount() {
		return accessCount;
	}

	public byte[] getData() {
		return accessibleData;
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

	public short getCapacity() {
		return (short) accessibleData.length;
	}
	
	public short headerSize() {
		return 0;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
}
