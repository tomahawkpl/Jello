package com.atteo.jello.store;



/**
 * Page which can be organised into single linked lists
 * 
 * @author tomahawk
 * 
 */
public class ListPage extends Page {
	public ListPage() {
		super();
	}

	public int getNext() {
		return (data[0] << 24)
        + ((data[1] & 0xFF) << 16)
        + ((data[2] & 0xFF) << 8)
        + (data[3] & 0xFF);
	}

	@Override
	public short headerSize() {
		return Integer.SIZE;
	}

	public void setNext(final int id) {
		data[0] = (byte)(id >>> 24);
		data[1] = (byte)(id >>> 16);
		data[2] = (byte)(id >>> 8);
		data[3] = (byte)id;
	}

}
