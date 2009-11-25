package com.atteo.jello.store;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class StoreModule implements Module {
	private final int pagePoolLimit = 5;
	private final int pageSize = PagedFile.getPageSize();
	private final Map<String, String> properties;

	public StoreModule(final Map<String, String> properties) {
		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);
	}

	public void configure(final Binder binder) {
		Names.bindProperties(binder, properties);
		binder.bind(PagePool.class);


	}

	private Map<String, String> getDefaultProperties() {
		final Map<String, String> p = new HashMap<String, String>();
		p.put("pagePoolLimit", String.valueOf(pagePoolLimit));
		p.put("pageSize", String.valueOf(pageSize));
		return p;
	}

}
