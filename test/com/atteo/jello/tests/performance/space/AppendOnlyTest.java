package com.atteo.jello.tests.performance.space;

import com.atteo.jello.space.AppendOnly;
import com.atteo.jello.space.AppendOnlyCache;
import com.atteo.jello.space.AppendOnlyCacheNative;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.google.inject.Binder;
import com.google.inject.name.Names;

public class AppendOnlyTest extends SpaceManagerPolicyTest {
	private final int appendOnlyCacheSize = 8;

	@Override
	public void configure(final Binder binder) {
		super.configure(binder);
		binder.bind(AppendOnlyCache.class).to(AppendOnlyCacheNative.class);
		binder.bind(Integer.class).annotatedWith(
				Names.named("appendOnlyCacheSize")).toInstance(
				appendOnlyCacheSize);
	}

	@Override
	protected Class<? extends SpaceManagerPolicy> implementation() {
		return AppendOnly.class;
	}

}
