package com.atteo.jello.schema;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import com.atteo.jello.Storable;

public class VanillaStorableWriter implements StorableWriter {

	public void readStorable(byte[] data, Storable storable, Schema schema) {
		int[] fields = schema.fields;
		String names[] = schema.names;
		int field;

		int l = fields.length;

		ByteBuffer buffer = ByteBuffer.wrap(data);

		try {
			for (int i = 0; i < l; i++) {
				field = fields[i];
				Field f = null;
				try {
					f = storable.getStorableClass().getField(names[i]);
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
				
				if (field == Schema.FIELD_INT) {
					f.set(storable, buffer.getInt());
				}
				if (field == Schema.FIELD_STRING) {
					int strLen = buffer.getInt();
					int pos = buffer.position();
					f.set(storable, new String(data, pos,
							strLen));
					buffer.position(pos + strLen);
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	public int writeStorable(byte data[], Storable storable, Schema schema) {
		int fields[] = schema.fields;
		String names[] = schema.names;
		int field;

		int l = fields.length;
		ByteBuffer buffer = ByteBuffer.wrap(data);	
		try {
			for (int i = 0; i < l; i++) {
				field = fields[i];
				Field f = null;
				try {
					f = storable.getStorableClass().getField(names[i]);
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
				if (field == Schema.FIELD_INT)
					buffer.putInt(f.getInt(storable));
				if (field == Schema.FIELD_STRING) {
					String str = (String) f.get(storable);
					byte[] b = str.getBytes();
					buffer.putInt(b.length);
					buffer.put(b);
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return buffer.position();
	}

}
