package com.atteo.jello.space;

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
	public native short getFreeSpace(int id);
	public native int getBestId(short freeSpace);
	public native boolean isEmpty();
	public native void update(int id, short freeSpace);
}