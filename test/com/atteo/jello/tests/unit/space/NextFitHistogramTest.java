package com.atteo.jello.tests.unit.space;

import java.io.IOException;

import android.test.InstrumentationTestCase;

import com.atteo.jello.space.NextFitHistogram;
import com.atteo.jello.space.VanillaHistogram;

public class NextFitHistogramTest extends InstrumentationTestCase {
	private NextFitHistogram nextFitHistogram;

	public void testComplex() {
		nextFitHistogram.update(0, (short) -1, (short) 1024);
		nextFitHistogram.update(1, (short) -1, (short) 2048);

		assertEquals(0, nextFitHistogram.getWitness((short) 512));
		assertEquals(1, nextFitHistogram.getWitness((short) 1500));
	}

	public void testSimple() {
		// is initially empty
		assertEquals(NextFitHistogram.NO_PAGE, nextFitHistogram
				.getWitness((short) 1));

		// adding an empty page doesn't change anything
		nextFitHistogram.update(0, (short) -1, (short) -1);
		assertEquals(NextFitHistogram.NO_PAGE, nextFitHistogram
				.getWitness((short) 1));

		nextFitHistogram.update(0, (short) -1, (short) 100);

		assertEquals(NextFitHistogram.NO_PAGE, nextFitHistogram
				.getWitness((short) 100));
		assertEquals(NextFitHistogram.NO_PAGE, nextFitHistogram
				.getWitness((short) 101));
		assertEquals(NextFitHistogram.NO_PAGE, nextFitHistogram
				.getWitness((short) 512));
		nextFitHistogram.update(0, (short) 100, (short) 512);
		assertEquals(0, nextFitHistogram.getWitness((short) 0));
		assertEquals(NextFitHistogram.NO_PAGE, nextFitHistogram
				.getWitness((short) 512));

		nextFitHistogram.update(0, (short) 512, (short) 4096);
		assertEquals(0, nextFitHistogram.getWitness((short) 0));
		assertEquals(0, nextFitHistogram.getWitness((short) 512));
		assertEquals(0, nextFitHistogram.getWitness((short) 4096));

	}

	@Override
	protected void setUp() throws IOException {
		nextFitHistogram = new VanillaHistogram((short) 4096, 8);
	}

	@Override
	protected void tearDown() {
	}
}
