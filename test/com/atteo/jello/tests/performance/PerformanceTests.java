package com.atteo.jello.tests.performance;

import android.test.suitebuilder.TestSuiteBuilder;
import junit.framework.Test;
import junit.framework.TestSuite;

public class PerformanceTests extends TestSuite {
	public static Test suite() {
		return new TestSuiteBuilder(PerformanceTests.class).includeAllPackagesUnderHere().build();
	}
}
