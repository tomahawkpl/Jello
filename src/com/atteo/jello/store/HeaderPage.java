package com.atteo.jello.store;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class HeaderPage extends Page {
	static final private String MAGIC = "JelloDatabase";
	
	@Inject
	private HeaderPage(@Named("pageSize") final short pageSize) {
		super(pageSize);
		byteBuffer.put(MAGIC.getBytes());
	}
	
	public boolean load() {
		return true;
	}


}
