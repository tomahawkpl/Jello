package com.atteo.jello.schema;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import com.atteo.jello.Storable;

public class VanillaStorableWriter implements StorableWriter {

	public void readStorable(byte[] data, Storable storable, Schema schema) {
		Field[] fields = schema.fields;
		Field field;

		int l = fields.length;

		ByteBuffer buffer = ByteBuffer.wrap(data);

		try {
			for (int i = 0; i < l; i++) {
				field = fields[i];
				Class<?> type = field.getType();
				if (type == Integer.TYPE) {
					field.set(storable, buffer.getInt());
				}
				if (type == String.class) {
					int strLen = buffer.getInt();
					int pos = buffer.position();
					field.set(storable, new String(data, pos,
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

	public byte[] writeStorable(Storable storable, Schema schema) {
		Field[] fields = schema.fields;
		Field field;

		byte[] result = null;

		int l = fields.length;

		int space = 0;

		try {

			for (int i = 0; i < l; i++) {
				field = fields[i];
				Class<?> type = field.getType();
				if (type == Integer.TYPE) {
					space += Integer.SIZE / Byte.SIZE;;
					continue;
				}
				if (type == String.class) {
					space += Integer.SIZE / Byte.SIZE
							+ ((String) field.get(storable)).getBytes().length;
					continue;
				}
			}

			result = new byte[space];

			ByteBuffer buffer = ByteBuffer.wrap(result);

			for (int i = 0; i < l; i++) {
				field = fields[i];
				Class<?> type = field.getType();
				if (type == Integer.TYPE)
					buffer.putInt(field.getInt(storable));
				if (type == String.class) {
					String str = (String) field.get(storable);
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

		return result;
	}

}
