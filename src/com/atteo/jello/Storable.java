package com.atteo.jello;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.atteo.jello.schema.Schema;
import com.atteo.jello.transaction.TransactionManager;
import com.google.inject.Inject;

abstract public class Storable {
	static protected Schema schema = null;
	static protected Class<? extends Storable> thisClass = null;
	static protected FieldComparator comparator;
	static protected boolean isManaged, isSchemaManaged;
	
	@Inject static protected TransactionManager transactionManager;
	
	protected int id;
	
	protected Storable() {
		if (thisClass == null)
			thisClass = this.getClass();

		if (schema == null)
			schema = createClassSchema();
		
		if (comparator == null)
			comparator = new FieldComparator();

	}
	
	public Class<? extends Storable> getStorableClass() {
		return thisClass;
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
					fields.add(field);
				}
			}
			k = k.getSuperclass();
		}
		
		schema.fields = new Field[fields.size()];
		schema.fields = fields.toArray(schema.fields);
		Arrays.sort(schema.fields, comparator);

		return schema;
	}
	
	class FieldComparator implements Comparator<Field> {
		public int compare(Field field1, Field field2) {
			return field1.getName().compareTo(field2.getName());
		}
	}

	public Schema getSchema() {
		return schema;
	}
	
	public void save() {
		transactionManager.performInsertTransaction(this);		
	}

	public void load() {

	}
}