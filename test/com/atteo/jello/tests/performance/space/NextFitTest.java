package com.atteo.jello.tests.performance.space;

import com.atteo.jello.space.NextFit;
import com.atteo.jello.space.NextFitHistogram;
import com.atteo.jello.space.NextFitHistogramNative;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.google.inject.Binder;
import com.google.inject.name.Names;

public class NextFitTest extends SpaceManagerPolicyTest {
	private final int nextFitHistogramClasses = 8;

	@Override
	protected Class<? extends SpaceManagerPolicy> implementation() {
		return NextFit.class;
	}

	@Override
	public void configure(Binder binder) {
		super.configure(binder);

		binder.bind(NextFitHistogram.class).to(NextFitHistogramNative.class);
		binder.bind(Integer.class).annotatedWith(
				Names.named("nextFitHistogramClasses")).toInstance(
				nextFitHistogramClasses);

	}

}
