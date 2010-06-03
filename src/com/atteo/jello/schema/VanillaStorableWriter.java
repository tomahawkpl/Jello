package com.atteo.jello.schema;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import com.atteo.jello.Storable;

public class VanillaStorableWriter implements StorableWriter {

	public void readStorable(final byte[] data, final Storable storable,
			final Schema schema) {
		final int[] fields = schema.fields;
		final String names[] = schema.names;
		int field;

		final int l = fields.length;

		final ByteBuffer buffer = ByteBuffer.wrap(data);

		try {
			for (int i = 0; i < l; i++) {
				field = fields[i];
				Field f = null;
				f = storable.getDbField(names[i]);

				if (f == null)
					continue;

				if (field == Schema.FIELD_INT)
					f.set(storable, buffer.getInt());
				if (field == Schema.FIELD_STRING) {
					final int strLen = buffer.getInt();
					final int pos = buffer.position();
					f.set(storable, new String(data, pos, strLen));
					buffer.position(pos + strLen);
				}
			}
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	public int writeStorable(final byte data[], final Storable storable,
			final Schema schema) {
		final int fields[] = schema.fields;
		final String names[] = schema.names;
		int field;

		final int l = fields.length;
		final ByteBuffer buffer = ByteBuffer.wrap(data);
		try {
			for (int i = 0; i < l; i++) {
				field = fields[i];
				Field f = null;
				
				f = storable.getDbField(names[i]);

				if (f == null)
					throw new RuntimeException("Field " + names[i] + " not found when writing");
				
				if (field == Schema.FIELD_INT)
					buffer.putInt(f.getInt(storable));
				if (field == Schema.FIELD_STRING) {
					final String str = (String) f.get(storable);
					final byte[] b = str.getBytes();
					buffer.putInt(b.length);
					buffer.put(b);
				}
			}
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		}

		return buffer.position();
	}

}
