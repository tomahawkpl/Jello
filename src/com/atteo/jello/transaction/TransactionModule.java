package com.atteo.jello.transaction;

import java.util.HashMap;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class TransactionModule implements Module {

	// ---- SETTINGS

	// --------------

	private final HashMap<String, String> properties;

	public TransactionModule(final HashMap<String, String> properties) {
		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);

	}

	public void configure(final Binder binder) {
		binder.bind(TransactionManager.class)
				.to(SimpleTransactionManager.class);

		Names.bindProperties(binder, properties);
	}

	private HashMap<String, String> getDefaultProperties() {
		final HashMap<String, String> p = new HashMap<String, String>();

		return p;
	}
}
