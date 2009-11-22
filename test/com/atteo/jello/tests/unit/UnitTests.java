package com.atteo.jello.tests.unit;

import junit.framework.Test;
import junit.framework.TestSuite;
import android.test.suitebuilder.TestSuiteBuilder;

public class UnitTests extends TestSuite {
	public static Test suite() {
		return new TestSuiteBuilder(UnitTests.class)
				.includeAllPackagesUnderHere().build();
	}
}
