package com.atteo.jello.tests.unit;

import android.test.suitebuilder.TestSuiteBuilder;
import junit.framework.Test;
import junit.framework.TestSuite;

public class UnitTests extends TestSuite {
	public static Test suite() {
		return new TestSuiteBuilder(UnitTests.class).includeAllPackagesUnderHere().build();
	}
}
