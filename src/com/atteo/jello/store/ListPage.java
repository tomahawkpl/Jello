package com.atteo.jello.store;

/**
 * Page which can be organised into single linked lists
 * 
 * 
 */
public class ListPage extends Page {
	public static final int NO_MORE_PAGES = -1;

	public ListPage() {
		super();
	}

	public int getNext() {
		final int pos = byteBuffer.position();
		byteBuffer.position(0);
		final int i = byteBuffer.getInt();
		byteBuffer.position(pos);
		return i;
	}

	@Override
	public short headerSize() {
		return 4;
	}

	public void setNext(final int id) {
		final int pos = byteBuffer.position();
		byteBuffer.position(0);
		byteBuffer.putInt(id);
		byteBuffer.position(pos);
	}

}
