package com.atteo.jello.tests;

import java.io.IOException;

import android.test.InstrumentationTestCase;

import com.atteo.jello.misc.Grapher;
import com.atteo.jello.store.StoreModule;
import com.google.inject.Guice;

public class GenerateGraph extends InstrumentationTestCase {

	// dummy test just to run the graph generation on a device
	public void testGenerateGraph() throws IOException {
		Grapher.graph("/sdcard/jello/graph", Guice
				.createInjector(new StoreModule(null, null)));
	}

}