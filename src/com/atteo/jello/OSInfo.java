package com.atteo.jello;

public class OSInfo {
	static {
		System.loadLibrary("OSInfo");
	}
	
	native static public short getPageSize();
	
}
