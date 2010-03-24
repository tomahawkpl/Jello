package com.atteo.jello.store;

import java.util.HashMap;

import android.util.Pool;
import android.util.Pools;

import com.atteo.jello.DatabaseFile;
import com.atteo.jello.OSInfo;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class StoreModule implements Module {
	// ---- SETTINGS
	private final int pagePoolLimit = 5;
	private final String fullpath;
	// --------------

	private Pool<Page> pagePool = null;
	
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
		binder.bind(Short.class).annotatedWith(
				Names.named("pageSize")).toProvider(
				PageSizeProvider.class);
		binder.bind(HeaderPage.class);
		binder.bind(DatabaseFile.class);
		binder.bind(PagedFile.class).to(PagedFileRAF.class);
	}
	
	@Provides
	Pool<Page> pagePoolProvider(PagePoolableManager manager, @Named("pagePoolLimit") int limit) {
		if (pagePool == null)
			pagePool = Pools.finitePool(manager, limit);
		return pagePool;
	}

	private HashMap<String, String> getDefaultProperties() {
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("pagePoolLimit", String.valueOf(pagePoolLimit));
		p.put("fullpath", fullpath);

		return p;
	}

	public static class PageSizeProvider implements Provider<Short> {

		public Short get() {
			return OSInfo.getPageSize();
		}

	};

}
