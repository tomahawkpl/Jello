package com.atteo.jello.space;

public interface NextFitHistogram {
	public static int NO_WITNESS = -1;
	public static int NO_PAGE = -2;

	public short getClassSize();

	public int getWitness(short freeSpace);

	public void update(int id, short previousFreeSpace, short freeSpace);
}