package com.atteo.jello.tests.performance.space;

import com.atteo.jello.space.AppendOnly;
import com.atteo.jello.space.AppendOnlyCache;
import com.atteo.jello.space.AppendOnlyCacheNative;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;

public class AppendOnlyTest extends SpaceManagerPolicyTest {
	private final int appendOnlyCacheSize = 8;

	
	@Override
	protected Class<? extends SpaceManagerPolicy> implementation() {
		return AppendOnly.class;
	}
	
	@Override
	protected Module extraBindings() {
		final Module s = super.extraBindings();
		Module m = new Module() {
			public void configure(final Binder binder) {
				binder.bind(AppendOnlyCache.class).to(AppendOnlyCacheNative.class);
				binder.bind(Integer.class).annotatedWith(
						Names.named("appendOnlyCacheSize")).toInstance(
						appendOnlyCacheSize);
			}

		};

		
		
		if (s != null)
			m = Modules.combine(s, m);

		return m;
	}

	
	
}
