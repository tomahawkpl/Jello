package com.atteo.jello.space;

import java.util.ArrayList;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class VanillaHistogram implements NextFitHistogram {
	private int classSize;
	private ArrayList<Integer> witnesses;
	private ArrayList<Integer> counts;
	private int count;
	private int histogramClasses;
	
	@Inject
	public VanillaHistogram(@Named("pageSize") short pageSize,
			@Named("histogramClasses") int histogramClasses) {

		if (pageSize % histogramClasses != 0)
			throw new IllegalArgumentException("histogramClasses should divide pageSize");
		
		this.classSize = pageSize / histogramClasses;
		
		histogramClasses++; // empty pages have a separate class

		this.histogramClasses = histogramClasses;
		count = 0;
		
		witnesses = new ArrayList<Integer>();
		counts = new ArrayList<Integer>();
		
		for (int i = 0; i < histogramClasses; i++) {
			witnesses.add(-1);
			counts.add(0);
		}
		
	}

	public int getWitness(short freeSpace) {
		int loc = classFor(freeSpace);
		loc++;
		if (loc == histogramClasses)
			loc = histogramClasses - 1;
		
		boolean found = false;
		
		while (loc < histogramClasses) {
			if (counts.get(loc) > 0) {
				found = true;
				int w = witnesses.get(loc);
				if (w != -1)
					return w;
			}
			loc++;
		}
		
		if (found == false)
			return NextFitHistogram.NO_PAGE;
		
		return NextFitHistogram.NO_WITNESS;
	}

	public void update(int id, short previousFreeSpace, short freeSpace) {
		if (previousFreeSpace != -1) {
			int loc = classFor(previousFreeSpace);
			counts.set(loc, counts.get(loc) - 1);
			if (witnesses.get(loc) == id)
				witnesses.set(loc, -1);
			
			count--;
		}
		
		if (freeSpace == -1)
			return;
		
		int loc = classFor(freeSpace);
		counts.set(loc, counts.get(loc) + 1);
		witnesses.set(loc,id);
		count++;
	}

	private int classFor(int freeSpace) {
		return freeSpace / classSize;
	}
}
