package com.atteo.jello.tests;

import java.io.IOException;

import com.atteo.jello.misc.Grapher;
import com.atteo.jello.store.StoreModule;
import com.google.inject.Guice;

import android.test.InstrumentationTestCase;

public class GenerateGraph extends InstrumentationTestCase {

	// dummy test just to run the graph generation on device
	public void testGenerateGraph() throws IOException {
		Grapher.graph("/sdcard/jello/graph", Guice.createInjector(new StoreModule(null)));
	}



}