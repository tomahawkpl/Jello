package com.atteo.jello.klass;

import com.atteo.jello.index.Index;
import com.atteo.jello.schema.SchemaManager;

public interface KlassManager {
	public boolean load();
	public void create();
	public void commit();
	
	public void addKlass(String klassName);
	public boolean isKlassManaged(String klassName);
	public void removeKlass(String klassName);
	
	public int getIdFor(String klassName);
	public SchemaManager getSchemaManagerFor(String klassName);
	public Index getIndexFor(String klassName);
}
