package com.atteo.jello.store;

import java.util.HashMap;

import android.util.Pool;
import android.util.Pools;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class StoreModule implements Module {
	// ---- SETTINGS
	private final int pagePoolLimit = 5;
	private final String fullpath;
	// --------------

	private final HashMap<String, String> properties;

	public StoreModule(final String fullpath,
			final HashMap<String, String> properties) {
		this.fullpath = fullpath;
		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);
	}

	public void configure(final Binder binder) {
		Names.bindProperties(binder, properties);
		binder.bind(Short.class).annotatedWith(Names.named("pageSize"))
				.toProvider(PageSizeProvider.class);
		binder.bind(PagedFile.class).to(PagedFileNative.class);

		binder.requestStaticInjection(Page.class);

	}

	private HashMap<String, String> getDefaultProperties() {
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("pagePoolLimit", String.valueOf(pagePoolLimit));
		p.put("fullpath", fullpath);

		return p;
	}

	@Provides
	@Singleton
	Pool<Page> pagePoolProvider(final PagePoolableManager manager,
			@Named("pagePoolLimit") final int limit) {
		return Pools.finitePool(manager, limit);
	}

}
