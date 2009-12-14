package com.atteo.jello.store;

import com.google.inject.Inject;
import com.google.inject.name.Named;

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
	
	public int getCapacity() {
		return data.length - Long.SIZE;
	}
	
	

}
