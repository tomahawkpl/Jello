package com.atteo.jello.tests;

import java.util.HashMap;

import android.util.Pool;
import android.util.Pools;

import com.atteo.jello.Record;
import com.atteo.jello.RecordPoolableManager;
import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagePoolableManager;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class CommonBindings implements Module {

	public void configure(final Binder binder) {
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("pagePoolLimit", "8");
		p.put("recordPoolLimit", "8");
		Names.bindProperties(binder, p);
	}

	@Provides
	@Singleton
	Pool<Page> pagePoolProvider(final PagePoolableManager manager,
			@Named("pagePoolLimit") final int limit) {
		return Pools.finitePool(manager, limit);
	}

	@Provides
	@Singleton
	Pool<Record> recordPoolProvider(final RecordPoolableManager manager,
			@Named("recordPoolLimit") final int limit) {
		return Pools.finitePool(manager, limit);
	}

}
