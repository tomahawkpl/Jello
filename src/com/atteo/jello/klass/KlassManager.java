package com.atteo.jello.klass;

import com.atteo.jello.index.Index;
import com.atteo.jello.schema.SchemaManager;


public interface KlassManager {
	public void addKlass(String klassName);

	public void commit();

	public void create();

	public int getIdFor(String klassName);

	public Index getIndexFor(String klassName);

	public SchemaManager getSchemaManagerFor(String klassName);

	public boolean isKlassManaged(String klassName);

	public boolean load();

	public void removeKlass(String klassName);
}
