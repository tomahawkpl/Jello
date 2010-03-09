package com.atteo.jello.tests.unit.space;

import java.io.IOException;

import android.test.InstrumentationTestCase;

import com.atteo.jello.space.NextFitHistogram;
import com.atteo.jello.space.VanillaHistogram;

public class NextFitHistogramTest extends InstrumentationTestCase {
	private NextFitHistogram nextFitHistogram;

	
	@Override
	protected void setUp() throws IOException {
		nextFitHistogram = new VanillaHistogram(4096, 8);
	}
	
	public void testSimple() {
		// is initially empty
		for (int i=0;i<8;i++)
			assertEquals(NextFitHistogram.NO_PAGE, nextFitHistogram.getWitness(i));
		
		// adding an empty page doesn't change anything
		for (int i=0;i<10;i++)
			nextFitHistogram.update(i, 0, 0);
		for (int i=0;i<8;i++)
			assertEquals(NextFitHistogram.NO_PAGE, nextFitHistogram.getWitness(i));

		nextFitHistogram.update(0, 0, 100);
		
		assertEquals(0, nextFitHistogram.getWitness(0));
		
		nextFitHistogram.update(0, 100, 0);
		assertEquals(NextFitHistogram.NO_PAGE, nextFitHistogram.getWitness(0));
	}

	public void testComplex() {
		nextFitHistogram.update(0, 0, 1);
		nextFitHistogram.update(1, 0, 511);
		nextFitHistogram.update(1, 511, 512);
		
		assertEquals(NextFitHistogram.NO_WITNESS, nextFitHistogram.getWitness(0));
		assertEquals(1, nextFitHistogram.getWitness(512));
	}
	
	@Override
	protected void tearDown() {
	}
}
