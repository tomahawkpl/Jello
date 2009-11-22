package com.atteo.jello.tests.unit.store;

import junit.framework.Test;
import junit.framework.TestSuite;
import android.test.suitebuilder.TestSuiteBuilder;

import com.atteo.jello.tests.unit.UnitTests;

public class StoreSuite extends TestSuite {
	public static Test suite() {
		return new TestSuiteBuilder(UnitTests.class)
				.includeAllPackagesUnderHere().build();
	}
}
