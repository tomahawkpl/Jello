package com.atteo.jello.index;

import java.util.HashMap;

import com.atteo.jello.store.PageSizeProvider;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Names;

public class IndexModule implements Module {

	// ---- SETTINGS
	private final short bTreeLeafCapacity;
	private final short bTreeNodeCapacity;

	// --------------

	private final HashMap<String, String> properties;

	
	public IndexModule(final HashMap<String, String> properties) {
		int pageSize = new PageSizeProvider().get();
		bTreeLeafCapacity = (short) (pageSize - 16);
		bTreeNodeCapacity = (short) (pageSize - 16);
		
		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);
	}
	
	public void configure(Binder binder) {
		binder.bind(IndexFactory.class).toProvider(
			    FactoryProvider.newFactory(IndexFactory.class, BTree.class));
		
		
		Names.bindProperties(binder, properties);
	}

	private HashMap<String, String> getDefaultProperties() {
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("bTreeNodeCapacity", String.valueOf(bTreeNodeCapacity));
		p.put("bTreeLeafCapacity", String.valueOf(bTreeLeafCapacity));
		
		return p;

	}
	
}
