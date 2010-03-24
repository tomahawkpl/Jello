package com.atteo.jello.tests.unit.space;

import com.atteo.jello.space.Hybrid;
import com.atteo.jello.space.SpaceManagerPolicy;

public class HybridTest extends SpaceManagerPolicyTest {
	@Override
	protected Class<? extends SpaceManagerPolicy> implementation() {
		return Hybrid.class;
	}
}
