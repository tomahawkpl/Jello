package com.atteo.jello.store;

import java.nio.ByteBuffer;

import android.util.Poolable;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Page implements Poolable<Page> {
	private Page nextPoolable = null;
	protected int id;
	protected byte data[] = null;
	protected byte accessibleData[] = null;
	protected ByteBuffer byteBuffer;

	@Inject
	public Page(@Named("pageSize") final short pageSize) {
		data = new byte[pageSize];
		byteBuffer = ByteBuffer.wrap(data);
		byteBuffer.position(headerSize());
		accessibleData = byteBuffer.slice().array();
	}

	public short getCapacity() {
		return (short) accessibleData.length;
	}

	public byte[] getData() {
		return accessibleData;
	}

	public int getId() {
		return id;
	}

	public Page getNextPoolable() {
		return nextPoolable;
	}

	public short headerSize() {
		return 0;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void setNextPoolable(final Page element) {
		nextPoolable = element;
	}
}
