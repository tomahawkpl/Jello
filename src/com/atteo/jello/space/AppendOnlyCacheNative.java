package com.atteo.jello.space;

import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class AppendOnlyCacheNative implements AppendOnlyCache {

	static {
		System.loadLibrary("AppendOnlyCacheNative");
	}

	@Inject
	public AppendOnlyCacheNative(
			@Named("appendOnlyCacheSize") final int appendOnlyCacheSize) {
		Log.i("jello","constructor");
		init(appendOnlyCacheSize);
		Log.i("jello","constructor2");
	}

	public native int getBestId(short freeSpace);

	public native short getFreeSpace(int id);

	public native boolean isEmpty();

	public native void update(int id, short freeSpace);

	private native void init(int appendOnlyCacheSize);
}