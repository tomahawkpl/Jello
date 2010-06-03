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

	static protected Field[] dbFields = null;
	
	@Inject
	static protected TransactionManager transactionManager;
	@Inject
	static protected Pool<Record> recordPool;

	protected Record record;

	protected Storable() {
		if (thisClass == null)
			thisClass = this.getClass();

		if (comparator == null)
			comparator = new FieldComparator();

		if (schema == null)
			schema = createClassSchema();

		if (dbFields == null)
			dbFields = extractDbFields();
		
		record = recordPool.acquire();
		klassName = thisClass.getCanonicalName();
	}

	public String getClassName() {
		return klassName;
	}

	public int getId() {
		return record.getId();
	}

	public Record getRecord() {
		return record;
	}

	public Schema getSchema() {
		return schema;
	}

	public Class<? extends Storable> getStorableClass() {
		return thisClass;
	}

	public boolean load() {
		return transactionManager.performFindTransaction(this);
	}

	public void save() {
		transactionManager.performInsertTransaction(this);
	}

	public void setId(final int id) {
		record.setId(id);
	}

	public void setRecord(final Record record) {
		this.record = record;
	}

	private Schema createClassSchema() {
		final Schema schema = new Schema();

		final ArrayList<Field> fields = new ArrayList<Field>();
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

		final int l = fields.size();
		Field f[] = new Field[l];
		f = fields.toArray(f);
		Arrays.sort(f, comparator);

		schema.fields = new int[l];
		schema.names = new String[l];

		for (int i = 0; i < l; i++) {
			schema.fields[i] = Schema.getFieldType(f[i].getType());
			schema.names[i] = f[i].getName();
		}

		return schema;
	}

	Field[] extractDbFields() {
		final ArrayList<Field> fields = new ArrayList<Field>();
		Class<?> k = thisClass;
		Field field;
		while (!k.equals(Object.class)) {
			final Field[] declaredFields = k.getDeclaredFields();
			for (int i = declaredFields.length - 1; i >= 0; i--) {
				field = declaredFields[i];
				if (field.isAnnotationPresent(DatabaseField.class)) {
					field.setAccessible(true);
					fields.add(field);
				}
			}
			k = k.getSuperclass();
		}
		Field[] f = fields.toArray(new Field[fields.size()]);
		Arrays.sort(f, comparator);
		return f;
	}

	public Field getDbField(String name) {
		int len = dbFields.length;
		int left = 0;
		int right = len;
		
		int r;
		
		while ((r = dbFields[(right + left)/2].getName().compareTo(name)) != 0) {
			if (r < 0)
				left = (right + left) / 2 + 1;
			else
				right = (right + left) / 2 - 1;
				
				
			if (right < left)
				return null;
		}
		
		return dbFields[(right+left) / 2];
	}
	
	@Override
	protected void finalize() {
		recordPool.release(record);
	}

	private class FieldComparator implements Comparator<Field> {
		public int compare(final Field field1, final Field field2) {
			return field1.getName().compareTo(field2.getName());
		}
	}
}