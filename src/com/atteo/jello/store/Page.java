package com.atteo.jello.store;

import java.nio.ByteBuffer;

import android.util.Poolable;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Page implements Poolable<Page> {
	private Page nextPoolable = null;
	protected int id;
	protected byte data[] = null;
	protected ByteBuffer byteBuffer;
	protected @Inject
	@Named("pageSize")
	static short pageSize;

	public Page() {
		data = new byte[pageSize];
		byteBuffer = ByteBuffer.wrap(data);
		byteBuffer.position(headerSize());
	}

	public void advance(final int i) {
		byteBuffer.position(byteBuffer.position() + i);
	}

	public short getCapacity() {
		return (short) (pageSize - headerSize());
	}

	public byte[] getData() {
		return data;
	}

	public int getId() {
		return id;
	}

	public int getInt() {
		return byteBuffer.getInt();
	}

	public Page getNextPoolable() {
		return nextPoolable;
	}

	public String getString(final int length) {
		final String result = new String(data, byteBuffer.position(), length);
		advance(length);
		return result;
	}

	public short headerSize() {
		return 0;
	}

	public void position(final int position) {
		byteBuffer.position(headerSize() + position);
	}

	public void putInt(final int value) {
		byteBuffer.putInt(value);
	}

	public void putString(final String string) {
		byteBuffer.put(string.getBytes());
	}

	public void reset() {
		byteBuffer.position(headerSize());
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void setNextPoolable(final Page element) {
		nextPoolable = element;
	}
}
