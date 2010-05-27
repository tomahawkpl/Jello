package com.atteo.jello.schema;

import java.util.HashMap;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Names;

public class SchemaModule implements Module {

	// ---- SETTINGS

	// --------------

	private final HashMap<String, String> properties;

	public SchemaModule(final HashMap<String, String> properties) {
		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);

	}

	public void configure(Binder binder) {
		binder.bind(SchemaManagerFactory.class).toProvider(
				FactoryProvider.newFactory(SchemaManagerFactory.class,
						SimpleSchemaManager.class));
		binder.bind(StorableWriter.class).to(VanillaStorableWriter.class);
		
		Names.bindProperties(binder, properties);
	}

	private HashMap<String, String> getDefaultProperties() {
		final HashMap<String, String> p = new HashMap<String, String>();

		return p;

	}

}
