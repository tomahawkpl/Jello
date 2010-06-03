package com.atteo.jello.tests.performance.space;

import junit.framework.Test;
import junit.framework.TestSuite;
import android.test.suitebuilder.TestSuiteBuilder;

import com.atteo.jello.tests.unit.UnitTests;

public class SpaceSuite extends TestSuite {
	public static Test suite() {
		return new TestSuiteBuilder(UnitTests.class)
				.includeAllPackagesUnderHere().build();
	}
}
