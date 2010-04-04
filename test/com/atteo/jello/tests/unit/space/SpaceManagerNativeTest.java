package com.atteo.jello.tests.unit.space;

import com.atteo.jello.space.SpaceManager;
import com.atteo.jello.space.SpaceManagerNative;

public class SpaceManagerNativeTest extends SpaceManagerTest {
	@Override
	protected Class<? extends SpaceManager> implementation() {
		return SpaceManagerNative.class;
	}

}