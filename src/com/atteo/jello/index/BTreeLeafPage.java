package com.atteo.jello.index;

public class BTreeLeafPage extends BTreePage {
	public static final int NO_PARENT = -1;
	public static final int NO_NEIGHBOUR = -1;
	
	public BTreeLeafPage() {
		super();
	}

	@Override
	public short headerSize() {
		return (Integer.SIZE / Byte.SIZE) * 1;
	}

}
