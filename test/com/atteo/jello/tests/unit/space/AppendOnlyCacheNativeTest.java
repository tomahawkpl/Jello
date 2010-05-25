package com.atteo.jello.tests.unit.space;

import com.atteo.jello.space.AppendOnlyCache;
import com.atteo.jello.space.AppendOnlyCacheNative;

public class AppendOnlyCacheNativeTest extends AppendOnlyCacheTest {

	@Override
	protected Class<? extends AppendOnlyCache> implementation() {
		return AppendOnlyCacheNative.class;
	}

}
