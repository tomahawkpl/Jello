package com.atteo.jello.tests.unit.schema;

import android.test.suitebuilder.TestSuiteBuilder;

import com.atteo.jello.tests.unit.UnitTests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class SchemaSuite extends TestSuite {
	public static Test suite() {
		return new TestSuiteBuilder(UnitTests.class)
				.includeAllPackagesUnderHere().build();
	}
}
