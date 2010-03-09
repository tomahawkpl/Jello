package com.atteo.jello.tests.performance.space;

import java.io.IOException;

import android.os.Debug;
import android.test.InstrumentationTestCase;
import android.test.PerformanceTestCase;

import com.atteo.jello.space.AppendOnlyCache;
import com.atteo.jello.space.AppendOnlyCacheNative;

public class AppendOnlyCacheTest extends InstrumentationTestCase implements
		PerformanceTestCase {
	private AppendOnlyCache appendOnlyCache;
	
	public boolean isPerformanceOnly() {
		return true;
	}

	public int startPerformance(final Intermediates intermediates) {

		return 1;
	}

	public void testUpdate() throws IOException {
		final int TESTSIZE = 100;
		Debug.startMethodTracing("jello/testAppendOnlyCache");
		for (int i=0;i<TESTSIZE;i++) {
			appendOnlyCache.update(4*i, 100);
			appendOnlyCache.update(4*i+1, 50);
			appendOnlyCache.update(4*i+2, 200);
			appendOnlyCache.update(4*i+3, 20);
		}
		Debug.stopMethodTracing();
		
	}


	@Override
	protected void setUp() throws IOException {
		appendOnlyCache = new AppendOnlyCacheNative(3);

	}
	
	@Override
	protected void tearDown() {
		
	}

}
