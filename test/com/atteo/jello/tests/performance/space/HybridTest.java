package com.atteo.jello.tests.performance.space;

import com.atteo.jello.space.Hybrid;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.google.inject.Binder;
import com.google.inject.name.Names;

public class HybridTest extends SpaceManagerPolicyTest {
	private final int nextFitHistogramClasses = 8;
	private final int appendOnlyCacheSize = 8;

	@Override
	public void configure(final Binder binder) {
		super.configure(binder);
		binder.bind(Integer.class).annotatedWith(
				Names.named("appendOnlyCacheSize")).toInstance(
				appendOnlyCacheSize);
		binder.bind(Integer.class).annotatedWith(
				Names.named("nextFitHistogramClasses")).toInstance(
				nextFitHistogramClasses);

	}

	@Override
	protected Class<? extends SpaceManagerPolicy> implementation() {
		return Hybrid.class;
	}

}
