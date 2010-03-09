package com.atteo.jello.space;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class NextFitHistogramNative implements NextFitHistogram {

	@Inject
	public NextFitHistogramNative(@Named("histogramClasses") int histogramClasses) {
	
	}
	
	public long getPagesWithFreeSpace(int freeSpace) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getWitness(int freeSpace) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void update(long id, int previousFreeSpace, int freeSpace) {
		// TODO Auto-generated method stub

	}

}
