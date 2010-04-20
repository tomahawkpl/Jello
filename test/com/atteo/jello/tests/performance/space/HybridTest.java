package com.atteo.jello.tests.performance.space;

import com.atteo.jello.space.AppendOnlyCache;
import com.atteo.jello.space.AppendOnlyCacheNative;
import com.atteo.jello.space.Hybrid;
import com.atteo.jello.space.NextFitHistogram;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.space.VanillaHistogram;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class HybridTest extends SpaceManagerPolicyTest {

	@Override
	protected Class<? extends SpaceManagerPolicy> implementation() {
		return Hybrid.class;
	}
	
	@Override
	protected Module extraBindings() {
		final Module s = super.extraBindings();
		Module m = new Module() {
			public void configure(final Binder binder) {
				binder.bind(NextFitHistogram.class).to(VanillaHistogram.class);
				binder.bind(AppendOnlyCache.class).to(AppendOnlyCacheNative.class);
			}

		};

		if (s != null)
			m = Modules.combine(s, m);

		return m;
	}

}
