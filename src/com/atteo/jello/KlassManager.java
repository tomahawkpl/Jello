package com.atteo.jello;

import com.atteo.jello.index.Index;
import com.atteo.jello.schema.SchemaManager;

public interface KlassManager {
	public boolean load();
	public void create();
	public void commit();
	
	public void addKlass(Class<? extends Storable> klass);
	public boolean isKlassManaged(Class<? extends Storable> klass);
	public void removeKlass(Class<? extends Storable> klass);
	
	public int getIdFor(Class<? extends Storable> klass);
	public SchemaManager getSchemaManagerFor(Class<? extends Storable> klass);
	public Index getIndexFor(Class<? extends Storable> klass);
}
