package com.atteo.jello.tests.performance;

import junit.framework.Test;
import junit.framework.TestSuite;
import android.test.suitebuilder.TestSuiteBuilder;

public class PerformanceTests extends TestSuite {
	public static Test suite() {
		return new TestSuiteBuilder(PerformanceTests.class)
				.includeAllPackagesUnderHere().build();
	}
}
