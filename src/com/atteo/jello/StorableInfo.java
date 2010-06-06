package com.atteo.jello;

import java.lang.reflect.Field;
import java.util.HashMap;

import com.atteo.jello.schema.Schema;

class StorableInfo {
	static private HashMap<Class<? extends Storable>, Schema> schemas = new HashMap<Class<? extends Storable>, Schema>();
	static private HashMap<Class<? extends Storable>, Field[]> dbFields = new HashMap<Class<? extends Storable>, Field[]>();
	static private HashMap<Class<? extends Storable>, Field[]> belongsToFields = new HashMap<Class<? extends Storable>, Field[]>();

	
	static Schema getClassSchema(Class<? extends Storable> klass, Storable storable) {
		if (!schemas.containsKey(klass))
			schemas.put(klass, storable.createClassSchema());
		return schemas.get(klass);
	}
	
	static Field[] getDbFields(Class<? extends Storable> klass, Storable storable) {
		if (!dbFields.containsKey(klass))
			dbFields.put(klass, storable.extractDbFields());
		return dbFields.get(klass);
	}
	
	static Field[] getBelongsToFields(Class<? extends Storable> klass, Storable storable) {
		if (!belongsToFields.containsKey(klass))
			belongsToFields.put(klass, storable.extractBelongsToFields());
		return belongsToFields.get(klass);
	}
}
