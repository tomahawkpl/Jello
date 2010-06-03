package com.atteo.jello.tests.unit.space;

import com.atteo.jello.space.NextFitHistogram;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.name.Names;

public abstract class NextFitHistogramTest extends
		JelloInterfaceTestCase<NextFitHistogram> {

	private final short pageSize = 4096;
	private final int nextFitHistogramClasses = 8;

	@Inject
	private NextFitHistogram nextFitHistogram;

	public void configure(final Binder binder) {
		binder.bind(Integer.class).annotatedWith(
				Names.named("nextFitHistogramClasses")).toInstance(
				nextFitHistogramClasses);
		binder.bind(Short.class).annotatedWith(Names.named("pageSize"))
				.toInstance(pageSize);
	}

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
	protected Class<NextFitHistogram> interfaceUnderTest() {
		return NextFitHistogram.class;
	}

	@Override
	protected void setUp() {
		super.setUp();
	}

	@Override
	protected void tearDown() {
	}
}
