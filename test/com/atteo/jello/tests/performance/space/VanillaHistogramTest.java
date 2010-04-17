package com.atteo.jello.tests.performance.space;

import com.atteo.jello.space.NextFitHistogram;
import com.atteo.jello.space.VanillaHistogram;

public class VanillaHistogramTest extends NextFitHistogramTest {

	@Override
	protected Class<? extends NextFitHistogram> implementation() {
		return VanillaHistogram.class;
	}

}
