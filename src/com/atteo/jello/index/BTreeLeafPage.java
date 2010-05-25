package com.atteo.jello.index;

public class BTreeLeafPage extends BTreePage {
	public static final int NO_PARENT = -1;
	public static final int NO_NEIGHBOUR = -1;
	
	public BTreeLeafPage() {
		super();
	}

	public int getLeft() {
		return getByteValue(1);
	}
	
	public int getRight() {
		return getByteValue(2);
	}

	@Override
	public short headerSize() {
		return (Integer.SIZE / Byte.SIZE) * 3;
	}

	public void setLeft(final int id) {
		setByteValue(id, 1);
	}
	
	public void setRight(final int id) {
		setByteValue(id, 2);
	}
}
