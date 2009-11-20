package com.atteo.jello;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Names;

public class StoreModule implements Module {
	private final String bufferSize = "100";
	private final String pageSize = "4096";
	private final Map<String, String> properties;

	public StoreModule(Map<String, String> properties) {
		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);
	}

	private Map<String, String> getDefaultProperties() {
		Map<String, String> p = new HashMap<String, String>();
		p.put("pageSize", pageSize);
		p.put("bufferSize", bufferSize);
		return p;
	}

	@Override
	public void configure(Binder binder) {
		Names.bindProperties(binder, properties);
		binder.bind(RawPagedFileFactory.class).toProvider(
				FactoryProvider.newFactory(RawPagedFileFactory.class,
						RawPagedFile.class));

	}

}
