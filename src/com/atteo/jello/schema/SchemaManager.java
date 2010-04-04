package com.atteo.jello.schema;

import com.atteo.jello.Storable;

public interface SchemaManager {
	public void create();
	public void load();
	
	public int addSchema(Schema schema);
	public void removeSchema(int version);
	public Schema getSchema(int version);

	public Schema getSchemaForClass(Class<? extends Storable> klass);
}
