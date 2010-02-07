package com.atteo.jello.store;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Page which can be organised into single linked lists
 * @author tomahawk
 *
 */
public class ListPage extends Page {	
	@Inject
	ListPage(@Named("pageSize") final int pageSize) {	
		super(pageSize);
	}
	
	public void setNext(long id) {
		byteBuffer.putLong(0, id);
	}
	
	public long getNext() {
		return byteBuffer.getLong(0);
	}
	
	public int getDataStart() {
		return Long.SIZE;
	}
	
	public int getCapacity() {
		return data.length - Long.SIZE;
	}
	
	

}
