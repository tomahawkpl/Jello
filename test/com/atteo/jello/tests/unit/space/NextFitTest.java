package com.atteo.jello.tests.unit.space;

import com.atteo.jello.space.NextFit;
import com.atteo.jello.space.NextFitHistogram;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.space.VanillaHistogram;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;

public class NextFitTest extends SpaceManagerPolicyTest {
	private final int nextFitHistogramClasses = 8;
	
	
	@Override
	protected Class<? extends SpaceManagerPolicy> implementation() {
		return NextFit.class;
	}

	@Override
	protected Module extraBindings() {
		final Module s = super.extraBindings();
		Module m = new Module() {
			public void configure(final Binder binder) {
				binder.bind(NextFitHistogram.class).to(VanillaHistogram.class);
				binder.bind(Integer.class).annotatedWith(
						Names.named("nextFitHistogramClasses")).toInstance(
						nextFitHistogramClasses);
			}

		};

		if (s != null)
			m = Modules.combine(s, m);

		return m;
	}
	
}
