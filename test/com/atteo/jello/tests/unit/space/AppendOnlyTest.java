package com.atteo.jello.tests.unit.space;

import com.atteo.jello.space.AppendOnly;
import com.atteo.jello.space.AppendOnlyCache;
import com.atteo.jello.space.AppendOnlyCacheNative;
import com.atteo.jello.space.SpaceManager;
import com.atteo.jello.space.SpaceManagerNative;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.google.inject.Binder;
import com.google.inject.name.Names;

public class AppendOnlyTest extends SpaceManagerPolicyTest {
	private static final int appendOnlyCacheSize = 4;
	
	@Override
	protected Class<? extends SpaceManagerPolicy> implementation() {
		return AppendOnly.class;
	}
	
	@Override
	public void configure(Binder binder) {
		super.configure(binder);
		binder.bind(SpaceManager.class).to(SpaceManagerNative.class);
		binder.bind(AppendOnlyCache.class).to(AppendOnlyCacheNative.class);
		binder.bind(Integer.class).annotatedWith(
				Names.named("appendOnlyCacheSize")).toInstance(
				appendOnlyCacheSize);
	}
}