package com.atteo.jello.schema;


public interface SchemaManager {
	public int addSchema(Schema schema);

	public void commit();

	public void create();

	public Schema getSchema(int version);

	public boolean load();

	public void removeSchema(int version);
}
