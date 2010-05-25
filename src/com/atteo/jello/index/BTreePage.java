package com.atteo.jello.index;

import com.atteo.jello.store.Page;

public abstract class BTreePage extends Page {
	public static final int NO_PARENT = -1;

	
	public BTreePage() {
		super();
	}

	public int getParent() {
		return getByteValue(0);
	}

	@Override
	public short headerSize() {
		return Integer.SIZE / Byte.SIZE;
	}

	public void setParent(final int id) {
		setByteValue(id, 0);
	}

	protected void setByteValue(int value, int position) {
		position *= Integer.SIZE;
		data[position + 0] = (byte)(id >>> 24);
		data[position + 1] = (byte)(id >>> 16);
		data[position + 2] = (byte)(id >>> 8);
		data[position + 3] = (byte)id;
	}
	
	protected int getByteValue(int position) {
		position *= Integer.SIZE;
		return (data[position + 0] << 24)
        + ((data[position + 1] & 0xFF) << 16)
        + ((data[position + 2] & 0xFF) << 8)
        + (data[position + 3] & 0xFF);
	}
}
