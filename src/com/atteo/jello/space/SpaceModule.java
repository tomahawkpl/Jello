package com.atteo.jello.space;

import java.util.HashMap;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class SpaceModule implements Module {
	// ---- SETTINGS

	private final int appendOnlyCacheSize = 8;
	private final int histogramClasses = 8;
	private final int hybridThreshold = 90;

	// --------------

	private final HashMap<String, String> properties;

	public SpaceModule(final HashMap<String, String> properties) {
		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);
		
	}

	public void configure(final Binder binder) {
		Names.bindProperties(binder, properties);
		
		binder.bind(AppendOnlyCache.class).to(AppendOnlyCacheNative.class);
		binder.bind(NextFitHistogram.class).to(VanillaHistogram.class);
	}

	private HashMap<String, String> getDefaultProperties() {
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("appendOnlyCacheSize", String.valueOf(appendOnlyCacheSize));
		p.put("histogramClasses", String.valueOf(histogramClasses));
		p.put("hybridThreshold", String.valueOf(hybridThreshold));

		return p;
	}

}
