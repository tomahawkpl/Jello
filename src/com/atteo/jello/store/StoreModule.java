package com.atteo.jello.store;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Names;

public class StoreModule implements Module {
	private final int bufferSize = 100;
	private final int pageSize = 4096;
	private final int pagePoolLimit = 5;
	private final int fileSizeLimit = 1024 * 1024 * 100;
	private final Map<String, String> properties;

	public StoreModule(Map<String, String> properties) {
		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);
	}

	private Map<String, String> getDefaultProperties() {
		Map<String, String> p = new HashMap<String, String>();
		p.put("pageSize", String.valueOf(pageSize));
		p.put("bufferSize", String.valueOf(bufferSize));
		p.put("pagePoolLimit", String.valueOf(pagePoolLimit));
		p.put("fileSizeLimit", String.valueOf(fileSizeLimit));
		return p;
	}

	@Override
	public void configure(Binder binder) {
		Names.bindProperties(binder, properties);
		binder.bind(PagePoolManager.class);
		binder.bind(PagePool.class);
		binder.bind(OSFileFactory.class).toProvider(
				FactoryProvider.newFactory(OSFileFactory.class,
						OSFileRAF.class));
		binder.bind(RawPagedFileFactory.class).toProvider(
				FactoryProvider.newFactory(RawPagedFileFactory.class,
						RawPagedFile.class));

	}

}
