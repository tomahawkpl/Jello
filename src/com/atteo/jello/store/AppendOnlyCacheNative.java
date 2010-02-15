package com.atteo.jello.store;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class AppendOnlyCacheNative implements AppendOnlyCache {

	static {
		System.loadLibrary("AppendOnlyCacheNative");
	}
	
	@Inject
	public AppendOnlyCacheNative(@Named("appendOnlyCacheSize") int appendOnlyCacheSize) {
		init(appendOnlyCacheSize);
	}
	
	private native void init(int appendOnlyCacheSize);
	public native int getBestFreeSpace();
	public native long getBestId();
	public native boolean isEmpty();
	public native void update(long id, int freeSpace);
}
