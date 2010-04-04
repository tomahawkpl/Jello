package com.atteo.jello.tests.unit.space;

import com.atteo.jello.space.AppendOnly;
import com.atteo.jello.space.SpaceManagerPolicy;

public class AppendOnlyTest extends SpaceManagerPolicyTest {
	@Override
	protected Class<? extends SpaceManagerPolicy> implementation() {
		return AppendOnly.class;
	}
}