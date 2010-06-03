package com.atteo.jello.tests.performance.space;

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
	NextFitHistogram histogram;

	public void configure(final Binder binder) {
		binder.bind(Integer.class).annotatedWith(
				Names.named("nextFitHistogramClasses")).toInstance(
				nextFitHistogramClasses);
		binder.bind(Short.class).annotatedWith(Names.named("pageSize"))
				.toInstance(pageSize);

	}

	@Override
	public void setUp() {
		super.setUp();
	}

	public void testUpdate() {
		final int TESTSIZE = 250;

		startPerformanceTest(true);

		for (int i = 0; i < TESTSIZE; i++) {
			histogram.update(4 * i, (short) 0, (short) 512);
			histogram.update(4 * i + 1, (short) 0, (short) 1024);
			histogram.update(4 * i + 2, (short) 0, (short) 2048);
			histogram.update(4 * i + 3, (short) 0, (short) 4096);
		}

		endPerformanceTest();
	}

	@Override
	protected Class<NextFitHistogram> interfaceUnderTest() {
		return NextFitHistogram.class;
	}

}
