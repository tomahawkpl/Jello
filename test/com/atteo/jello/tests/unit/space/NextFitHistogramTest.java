package com.atteo.jello.tests.unit.space;

import java.io.IOException;

import android.test.InstrumentationTestCase;

import com.atteo.jello.space.NextFitHistogram;
import com.atteo.jello.space.VanillaHistogram;

public class NextFitHistogramTest extends InstrumentationTestCase {
	private NextFitHistogram nextFitHistogram;

	
	@Override
	protected void setUp() throws IOException {
		nextFitHistogram = new VanillaHistogram((short)4096, 8);
	}
	
	public void testSimple() {
		// is initially empty
		for (int i=0;i<8;i++)
			assertEquals(NextFitHistogram.NO_PAGE, nextFitHistogram.getWitness((short) i));
		
		// adding an empty page doesn't change anything
		for (int i=0;i<10;i++)
			nextFitHistogram.update(i, (short)-1, (short)-1);
		for (int i=0;i<8;i++)
			assertEquals(NextFitHistogram.NO_PAGE, nextFitHistogram.getWitness((short) i));

		nextFitHistogram.update(0, (short)-1, (short)100);
		
		assertEquals(0, nextFitHistogram.getWitness((short) 0));
		
		nextFitHistogram.update(0, (short)100, (short) -1);
		assertEquals(NextFitHistogram.NO_PAGE, nextFitHistogram.getWitness((short) 0));
	}

	public void testComplex() {
		nextFitHistogram.update(0, (short)-1, (short)1);
		nextFitHistogram.update(1, (short)-1, (short)511);
		nextFitHistogram.update(1, (short)511, (short)512);
		
		assertEquals(NextFitHistogram.NO_WITNESS, nextFitHistogram.getWitness((short) 0));
		assertEquals(1, nextFitHistogram.getWitness((short) 512));
	}
	
	@Override
	protected void tearDown() {
	}
}
