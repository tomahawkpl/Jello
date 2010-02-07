package com.atteo.jello.store;

import java.util.HashMap;

import com.atteo.jello.space.AppendOnly;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class StoreModule implements Module {
	
	// ---- SETTINGS
	private final int pageSize = OSInfo.getPageSize();
	private final int pagePoolLimit = 5;
	private final int blockSize = 128;
	private final int appendOnlyCacheSize = 8;
	private final String fullpath;
	// --------------
	
	private final HashMap<String, String> properties;

	public StoreModule(final String fullpath, final HashMap<String, String> properties) {
		this.fullpath = fullpath;
		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);
	}

	public void configure(final Binder binder) {
		Names.bindProperties(binder, properties);
		binder.bind(PagePool.class);
		binder.bind(HeaderPage.class);
		binder.bind(DatabaseFile.class);
		binder.bind(PagedFile.class).to(PagedFileFast.class);
		binder.bind(SpaceManagerPolicy.class).to(AppendOnly.class);
	}

	private HashMap<String, String> getDefaultProperties() {
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("pagePoolLimit", String.valueOf(pagePoolLimit));
		p.put("pageSize", String.valueOf(pageSize));
		p.put("blockSize", String.valueOf(blockSize));
		p.put("fullpath",fullpath);
		p.put("appendOnlyCacheSize", String.valueOf(appendOnlyCacheSize));

		return p;
	}

}
