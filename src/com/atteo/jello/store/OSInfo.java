package com.atteo.jello.store;

public class OSInfo {
	static {
		System.loadLibrary("OSInfo");
	}
	
	native static public int getPageSize();
	
}
