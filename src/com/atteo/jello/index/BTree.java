package com.atteo.jello.index;

import com.atteo.jello.Record;
import com.google.inject.Inject;

public class BTree implements Index {

	static {
		System.loadLibrary("BTree");
	}
	
	@Inject
	public BTree() {
		init();
	}
	
	private native void init();
	public native void create(int pageId);
	public native void load(int pageId);
	public native void delete(Record record);
	public native void insert(Record record);
	public native Record find(int id);
	public native void update(Record record);
}
