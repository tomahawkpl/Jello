package com.atteo.jello.store;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Page which can be organised into single linked lists
 * 
 * @author tomahawk
 * 
 */
public class ListPage extends Page {
	@Inject
	public ListPage(@Named("pageSize") final short pageSize) {
		super(pageSize);
	}

	public int getNext() {
		return byteBuffer.getInt(0);
	}

	@Override
	public short headerSize() {
		return Integer.SIZE;
	}

	public void setNext(final int id) {
		byteBuffer.putInt(0, id);
	}

}
