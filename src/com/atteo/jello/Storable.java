package com.atteo.jello;

import com.atteo.jello.schema.Schema;
import com.atteo.jello.schema.SchemaManager;

abstract public class Storable {
	static protected Schema schema = null;
	static protected Class<? extends Storable> thisClass = null;
	
	protected Storable(SchemaManager schemaManager) {
		if (thisClass == null)
			thisClass = this.getClass();
		
		if (schema == null)
			schema = schemaManager.getSchemaForClass(this.getClass());
			
	}
	
	public void save() {
		
	}
	
	public void load() {
		
	}
}