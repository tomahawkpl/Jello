package com.atteo.jello.space;

import java.util.ArrayList;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class VanillaHistogram implements NextFitHistogram {
	private int classSize;
	private ArrayList<Long> witnesses;
	private ArrayList<Long> counts;

	@Inject
	public VanillaHistogram(@Named("pageSize") int pageSize,
			@Named("histogramClasses") int histogramClasses) {

		this.classSize = pageSize / histogramClasses;
		
		witnesses = new ArrayList<Long>();
		counts = new ArrayList<Long>();
		
		for (int i = 0; i < histogramClasses; i++) {
			witnesses.add((long) -1);
			counts.add((long)0);
		}
		
	}

	public long getWitness(int freeSpace) {
		int loc = classFor(freeSpace);
		if (counts.get(loc) == 0)
			return NextFitHistogram.NO_PAGE;
		
		long w = witnesses.get(loc);
		
		if (w == -1)
			return NextFitHistogram.NO_WITNESS;
		
		return w;
	}

	public void update(long id, int previousFreeSpace, int freeSpace) {
		if (previousFreeSpace > 0) {
			int loc = classFor(previousFreeSpace);
			counts.set(loc, counts.get(loc) - 1);
			
			if (witnesses.get(loc) == id)
				witnesses.set(loc, (long)-1);
		}
		
		if (freeSpace <= 0)
			return;
		
		int loc = classFor(freeSpace);
		counts.set(loc, counts.get(loc) + 1);
		witnesses.set(loc,id);
	}

	private int classFor(int freeSpace) {
		return freeSpace / classSize;
	}

}
