package com.atteo.jello.tests.performance.space;

import com.atteo.jello.space.NextFitHistogram;
import com.atteo.jello.space.NextFitHistogramNative;

public class NextFitHistogramNativeTest extends NextFitHistogramTest {

	@Override
	protected Class<? extends NextFitHistogram> implementation() {
		return NextFitHistogramNative.class;
	}

}
