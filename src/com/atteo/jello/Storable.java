package com.atteo.jello;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import android.util.Pool;

import com.atteo.jello.schema.Schema;
import com.atteo.jello.transaction.TransactionManager;
import com.google.inject.Inject;

abstract public class Storable {
	static protected Schema schema = null;
	static protected Class<? extends Storable> thisClass = null;
	static protected FieldComparator comparator;
	static protected boolean isManaged, isSchemaManaged;
	
	@Inject static protected TransactionManager transactionManager;
	@Inject static protected Pool<Record> recordPool;
	
	protected Record record;
	
	protected Storable() {
		if (thisClass == null)
			thisClass = this.getClass();

		if (comparator == null)
			comparator = new FieldComparator();
		
		if (schema == null)
			schema = createClassSchema();
		
		this.record = recordPool.acquire();

	}
	
	protected void finalize() {
		recordPool.release(this.record);
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
	
	private class FieldComparator implements Comparator<Field> {
		public int compare(Field field1, Field field2) {
			return field1.getName().compareTo(field2.getName());
		}
	}

	public Schema getSchema() {
		return schema;
	}
	
	public int getId() {
		return record.getId();
	}
	
	public void setId(int id) {
		this.record.setId(id);
	}
	
	public Record getRecord() {
		return record;
	}
	
	public void setRecord(Record record) {
		this.record = record;
	}
	
	public void save() {
		transactionManager.performInsertTransaction(this);		
	}

	public boolean load() {
		return transactionManager.performFindTransaction(this);
	}
}