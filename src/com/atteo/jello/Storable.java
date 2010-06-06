package com.atteo.jello;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import android.os.Bundle;
import android.util.Pool;

import com.atteo.jello.associations.BelongsTo;
import com.atteo.jello.associations.DatabaseField;
import com.atteo.jello.schema.Schema;
import com.atteo.jello.schema.StorableWriter;
import com.atteo.jello.transaction.TransactionManager;
import com.google.inject.Inject;
import com.google.inject.name.Named;

abstract public class Storable {
	protected Schema schema = null;
	protected String klassName = null;
	protected Class<? extends Storable> thisClass = null;
	static protected FieldComparator comparator;

	protected Field[] dbFields = null;
	protected Field[] belongsToFields = null;

	@Inject
	static protected StorableWriter storableWriter;
	@Inject
	static protected TransactionManager transactionManager;
	@Inject
	static protected Pool<Record> recordPool;
	@Inject
	static protected @Named("maxRecordPages") int maxRecordPages;
	@Inject
	static protected @Named("pageSize") short pageSize;
	
	protected Record record;

	private boolean isInDatabase;
	
	private byte bundleData[];
	
	protected Storable() {
		initClass();
	}

	protected Storable(int id) {
		initClass();
		setId(id);
	}

	private void initClass() {
		if (comparator == null)
			comparator = new FieldComparator();

		thisClass = this.getClass();

		if (dbFields == null)
			dbFields = StorableInfo.getDbFields(thisClass, this);

		if (belongsToFields == null)
			belongsToFields = StorableInfo.getBelongsToFields(thisClass, this);

		
		if (schema == null)
			schema = StorableInfo.getClassSchema(thisClass, this);
		
		record = recordPool.acquire();
		klassName = thisClass.getCanonicalName();
		
		bundleData = new byte[maxRecordPages * pageSize];
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

	@SuppressWarnings("unchecked")
	public <T> T load() {
		if (transactionManager.performFindTransaction(this)) {
			isInDatabase = true;
			return (T) this;
		} else {
			isInDatabase = false;
			return null;
		}
		
	}

	public void save() {
		if (!beforeSave())
			return;

		if (isInDatabase) {
			update();
			afterSave();
		} else {
			insert();
			afterSave();
		}
		isInDatabase = true;

		afterSave();
	}

	private void insert() {
		transactionManager.performInsertTransaction(this);
	}

	private void update() {
		transactionManager.performInsertTransaction(this);
	}

	public void remove() {
		transactionManager.performDeleteTransaction(this);
	}

	public void setId(final int id) {
		record.setId(id);
	}

	public void setRecord(final Record record) {
		this.record = record;
	}

	Schema createClassSchema() {
		final Schema schema = new Schema();

		int l = dbFields.length;
		int b = belongsToFields.length;

		schema.fields = new int[l + b];
		schema.names = new String[l + b];

		for (int i = 0; i < l; i++) {
			schema.fields[i] = Schema.getFieldType(dbFields[i].getType());
			schema.names[i] = dbFields[i].getName();
		}
		
		for (int i=0;i<b;i++) {
			schema.fields[i + l] = Schema.FIELD_STORABLE;
			schema.names[i + l] = belongsToFields[i].getName();
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
	
	Field[] extractBelongsToFields() {
		final ArrayList<Field> fields = new ArrayList<Field>();
		Class<?> k = thisClass;
		Field field;
		while (!k.equals(Object.class)) {
			final Field[] declaredFields = k.getDeclaredFields();
			for (int i = declaredFields.length - 1; i >= 0; i--) {
				field = declaredFields[i];
				if (field.isAnnotationPresent(BelongsTo.class)) {
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

	private Field findField(Field[] fields, String name) {
		int len = fields.length;
		int left = 0;
		int right = len;

		int r;

		while ((r = fields[(right + left) / 2].getName().compareTo(name)) != 0) {
			if (r < 0)
				left = (right + left) / 2 + 1;
			else
				right = (right + left) / 2 - 1;

			if (right < left)
				return null;
		}

		return fields[(right + left) / 2];

	}
	
	public Field getDbField(String name) {
		Field f = findField(dbFields, name);
		if (f == null)
			f = findField(belongsToFields,name);
		return f;
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

	public boolean loadBundle(final Bundle bundle) {
		if (!beforeLoadBundle())
			return false;
		if (bundle == null)
			throw new NullPointerException("Provided bundle is null");

		bundleData = bundle.getByteArray("content");
		
		if (bundleData == null)
			return false;
		
		storableWriter.readStorable(bundleData, this, schema);
		
		afterLoadBundle();
		return true;
	}

	public Bundle toBundle() {
		if (!beforeSaveToBundle())
			return null;
		final Bundle result = new Bundle();
		storableWriter.writeStorable(bundleData, this, schema);
		result.putByteArray("content", bundleData);
		
		afterSaveToBundle();
		return result;
	}

	protected void afterDelete() {

	}

	protected void afterLoad() {

	}

	protected void afterLoadBundle() {

	}

	protected void afterSave() {

	}

	protected void afterSaveToBundle() {

	}

	protected boolean beforeDelete() {
		return true;
	}

	protected boolean beforeLoad() {
		return true;
	}

	protected boolean beforeLoadBundle() {
		return true;
	}

	protected boolean beforeSave() {
		return true;
	}

	protected boolean beforeSaveToBundle() {
		return true;
	}
}
