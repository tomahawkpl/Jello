package com.atteo.jello;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.atteo.jello.schema.Schema;

abstract public class Storable {
	static protected Schema schema = null;
	static protected Class<? extends Storable> thisClass = null;

	protected int id;
	
	protected Storable() {
		if (thisClass == null)
			thisClass = this.getClass();

		if (schema == null)
			schema = createClassSchema();

	}
	
	private Schema createClassSchema() {
		Schema schema = new Schema();

		ArrayList<Field> fields = new ArrayList<Field>();
		Class<?> k = thisClass;
		Field field;
		Field[] declaredFields;
		while (!k.equals(Storable.class)) {
			declaredFields = k.getDeclaredFields();
			for (int i = declaredFields.length - 1; i >= 0; i--) {
				field = declaredFields[i];
				if (field.isAnnotationPresent(DatabaseField.class)) {
					field.setAccessible(true);
					if (field.getName() == "id")
						fields.add(0, field);
					else
						fields.add(field);
				}
			}
			k = k.getSuperclass();
		}

		schema.fields = new Field[fields.size()];
		schema.fields = fields.toArray(schema.fields);
		
		return schema;
	}

	public Schema getSchema() {
		return schema;
	}
	
	public void save() {

	}

	public void load() {

	}
}