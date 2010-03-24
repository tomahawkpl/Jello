package com.atteo.jello.tests;

import java.util.HashMap;

import android.util.Pool;
import android.util.Pools;

import com.atteo.jello.space.Record;
import com.atteo.jello.space.RecordPoolableManager;
import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagePoolableManager;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class CommonBindings implements Module {

	private Pool<Page> pagePool;
	private Pool<Record> recordPool;
	
	public void configure(Binder binder) {
		HashMap<String, String> p = new HashMap<String, String>();
		p.put("pagePoolLimit", "8");
		p.put("recordPoolLimit", "8");
		Names.bindProperties(binder, p);
	}
	
	@Provides
	Pool<Page> pagePoolProvider(PagePoolableManager manager, @Named("pagePoolLimit") int limit) {
		if (pagePool == null)
			pagePool = Pools.finitePool(manager, limit);
		return pagePool;
	}
	
	@Provides
	Pool<Record> recordPoolProvider(RecordPoolableManager manager, @Named("recordPoolLimit") int limit) {
		if (recordPool == null)
			recordPool = Pools.finitePool(manager, limit);
		return recordPool;
	}
	

}
