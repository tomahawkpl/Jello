package com.atteo.jello.misc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.grapher.GrapherModule;
import com.google.inject.grapher.InjectorGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import com.google.inject.grapher.graphviz.GraphvizRenderer;

public class Grapher {
	public static void graph(final String filename, final Injector demoInjector)
			throws IOException {
		final PrintWriter out = new PrintWriter(new File(filename), "UTF-8");

		final Injector injector = Guice.createInjector(new GrapherModule(),
				new GraphvizModule());
		final GraphvizRenderer renderer = injector
				.getInstance(GraphvizRenderer.class);
		renderer.setOut(out).setRankdir("TB");

		injector.getInstance(InjectorGrapher.class).of(demoInjector).graph();
	}
}