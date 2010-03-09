package com.atteo.jello.space;

public interface NextFitHistogram {
	public static int NO_WITNESS = -1;
	public static int NO_PAGE = -2;
	
	public long getWitness(int freeSpace);
	public void update(long id, int previousFreeSpace, int freeSpace);
}