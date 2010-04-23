package com.atteo.jello.space;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class NextFitHistogramNative implements NextFitHistogram {
	static {
		System.loadLibrary("NextFitHistogramNative");
	}

	@Inject
	public NextFitHistogramNative(@Named("pageSize") short pageSize,
			@Named("nextFitHistogramClasses") int histogramClasses) {
		init(pageSize, histogramClasses);
	}

	private native void init(short pageSize, int histogramClasses);

	public native short getClassSize();

	public native int getWitness(short freeSpace);

	public native void update(int id, short previousFreeSpace, short freeSpace);
}
