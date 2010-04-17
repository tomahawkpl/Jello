package com.atteo.jello.schema;


public interface SchemaManager {
	public void create();
	public void load();
	
	public int addSchema(Schema schema);
	public void removeSchema(int version);
	public Schema getSchema(int version);
}
