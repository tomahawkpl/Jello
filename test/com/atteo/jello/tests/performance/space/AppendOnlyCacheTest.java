package com.atteo.jello.tests.performance.space;

import java.io.IOException;

import com.atteo.jello.space.AppendOnlyCache;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.name.Names;

public abstract class AppendOnlyCacheTest extends
		JelloInterfaceTestCase<AppendOnlyCache> {
	
	private final int appendOnlyCacheSize = 8;
	
	@Inject
	private AppendOnlyCache appendOnlyCache;

	@Override
	protected Class<AppendOnlyCache> interfaceUnderTest() {
		return AppendOnlyCache.class;
	}

	public void configure(Binder binder) {
		binder.bind(Integer.class).annotatedWith(
				Names.named("appendOnlyCacheSize")).toInstance(
				appendOnlyCacheSize);
	}

	public void testUpdate() throws IOException {
		final int TESTSIZE = 250;

		startPerformanceTest(true);

		for (int i = 0; i < TESTSIZE; i++) {
			appendOnlyCache.update(4 * i, (short) 100);
			appendOnlyCache.update(4 * i + 1, (short) 50);
			appendOnlyCache.update(4 * i + 2, (short) 200);
			appendOnlyCache.update(4 * i + 3, (short) 20);
		}

		endPerformanceTest();
	}

	@Override
	protected void setUp() {
		super.setUp();
	}

	@Override
	protected void tearDown() {

	}

}
