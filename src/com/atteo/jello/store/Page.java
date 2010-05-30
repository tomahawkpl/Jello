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
	protected @Inject @Named("pageSize") static short pageSize;
	
	public Page() {
		data = new byte[pageSize];
		byteBuffer = ByteBuffer.wrap(data);
		byteBuffer.position(headerSize());
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
	
	public void reset() {
		byteBuffer.position(headerSize());
	}
	
	public void advance(int i) {
		byteBuffer.position(byteBuffer.position() + i);
	}
	
	public void position(int position) {
		byteBuffer.position(headerSize() + position);
	}
	
	public void putString(String string) {
		byteBuffer.put(string.getBytes());
	}

	public String getString(int length) {
		String result = new String(data, byteBuffer
				.position(), length);
		advance(length);
		return result;
	}
	
	public void putInt(int value) {
		byteBuffer.putInt(value);
	}
	
	public int getInt() {
		return byteBuffer.getInt();
	}
}
