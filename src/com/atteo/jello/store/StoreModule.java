package com.atteo.jello.store;

import java.util.HashMap;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Names;

public class StoreModule implements Module {
	private static final int pagePoolLimit = 5;
	private final int pageSize = OSInfo.getPageSize();
	private final HashMap<String, String> properties;

	public StoreModule(final HashMap<String, String> properties) {
		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);
	}

	public void configure(final Binder binder) {
		Names.bindProperties(binder, properties);
		binder.bind(PagePool.class);
		binder.bind(HeaderPage.class);
		binder.bind(PagedFile.Factory.class).toProvider(
				FactoryProvider.newFactory(PagedFile.Factory.class,
						PagedFileFast.class));
		binder.bind(DatabaseFile.Factory.class).toProvider(
				FactoryProvider.newFactory(DatabaseFile.Factory.class,
						DatabaseFile.class));

	}

	private HashMap<String, String> getDefaultProperties() {
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("pagePoolLimit", String.valueOf(pagePoolLimit));
		p.put("pageSize", String.valueOf(pageSize));
		return p;
	}

}
