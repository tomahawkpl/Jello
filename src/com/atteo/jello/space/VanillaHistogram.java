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
	private long freeSpaceSum;
	private int count;
	
	@Inject
	public VanillaHistogram(@Named("pageSize") short pageSize,
			@Named("histogramClasses") int histogramClasses) {

		this.classSize = pageSize / histogramClasses;
		count = 0;
		freeSpaceSum = 0;
		
		witnesses = new ArrayList<Integer>();
		counts = new ArrayList<Integer>();
		
		for (int i = 0; i < histogramClasses; i++) {
			witnesses.add(-1);
			counts.add(0);
		}
		
	}

	public int getWitness(short freeSpace) {
		int loc = classFor(freeSpace);
		if (counts.get(loc) == 0)
			return NextFitHistogram.NO_PAGE;
		
		int w = witnesses.get(loc);
		
		if (w == -1)
			return NextFitHistogram.NO_WITNESS;
		
		return w;
	}

	public void update(int id, short previousFreeSpace, short freeSpace) {
		freeSpaceSum += freeSpace - previousFreeSpace;
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

	public short averageFreeSpace() {
		return (short) (freeSpaceSum / count);
	}

}
