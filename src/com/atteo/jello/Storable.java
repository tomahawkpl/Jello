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
	static protected String klassName = null;
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
		klassName = thisClass.getCanonicalName();
	}
	
	protected void finalize() {
		recordPool.release(this.record);
	}
	
	public Class<? extends Storable> getStorableClass() {
		return thisClass;
	}
	
	public String getClassName() {
		return klassName;
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
		
		int l = fields.size();
		Field f[] = new Field[l];
		f = fields.toArray(f);
		Arrays.sort(f, comparator);

		schema.fields = new int[l];
		schema.names = new String[l];
		
		for (int i=0;i<l;i++) {
			schema.fields[i] = Schema.getFieldType(f[i].getClass());
			schema.names[i] = f[i].getName();
		}
		
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