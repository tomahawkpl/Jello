package com.atteo.jello.schema;

public interface SchemaContainer {
	public void setStoredClass(String klass);
	public String getStoredClass();
	
	public int addSchema(Schema schema);
	public Schema getSchema(int version);
}
