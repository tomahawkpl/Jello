package com.atteo.jello.schema;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Date;

import com.atteo.jello.Storable;
import com.atteo.jello.StorableFactory;

public class VanillaStorableWriter implements StorableWriter {

	@SuppressWarnings("unchecked")
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

				switch (field) {

				case Schema.FIELD_INT:
					f.set(storable, buffer.getInt());
					break;
				case Schema.FIELD_BYTE:
					f.set(storable, buffer.get());
					break;
				case Schema.FIELD_STRING:
					final int strLen = buffer.getInt();
					final int pos = buffer.position();
					f.set(storable, new String(data, pos, strLen));
					buffer.position(pos + strLen);
					break;
				case Schema.FIELD_SHORT:
					f.set(storable, buffer.getShort());
					break;
				case Schema.FIELD_BOOLEAN:
					f.set(storable, buffer.getInt() == 1);
					break;
				case Schema.FIELD_CHAR:
					f.set(storable, buffer.getChar());
					break;
				case Schema.FIELD_FLOAT:
					f.set(storable, buffer.getFloat());
					break;
				case Schema.FIELD_DOUBLE:
					f.set(storable, buffer.getDouble());
					break;
				case Schema.FIELD_LONG:
					f.set(storable, buffer.getLong());
					break;
				case Schema.FIELD_DATE:
					f.set(storable, milisToDate(buffer.getLong()));
					break;
				case Schema.FIELD_STORABLE:
					int id = buffer.getInt();
					if (id != StorableWriter.EMPTY_STORABLE) {
						Storable s = StorableFactory
								.createStorable((Class<? extends Storable>) f
										.getType());
						s.setId(id);
						f.set(storable, s);
					} else
						f.set(storable, null);
					break;

				default:
					throw new RuntimeException("Field type " + field
							+ " unsupported");
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
					throw new RuntimeException("Field " + names[i]
							+ " not found when writing");
				switch (field) {

				case Schema.FIELD_INT:
					buffer.putInt(f.getInt(storable));
					break;
				case Schema.FIELD_BYTE:
					buffer.put(f.getByte(storable));
					break;
				case Schema.FIELD_STRING:
					final String str = (String) f.get(storable);
					final byte[] b = str.getBytes();
					buffer.putInt(b.length);
					buffer.put(b);
					break;
				case Schema.FIELD_BOOLEAN:
					if (f.getBoolean(storable))
						buffer.putInt(1);
					else
						buffer.putInt(0);
					break;
				case Schema.FIELD_CHAR:
					buffer.putChar(f.getChar(storable));
					break;
				case Schema.FIELD_SHORT:
					buffer.putShort(f.getShort(storable));
					break;
				case Schema.FIELD_LONG:
					buffer.putLong(f.getLong(storable));
					break;
				case Schema.FIELD_FLOAT:
					buffer.putFloat(f.getFloat(storable));
					break;
				case Schema.FIELD_DOUBLE:
					buffer.putDouble(f.getDouble(storable));
					break;
				case Schema.FIELD_DATE:
					buffer.putLong(dateToMilis((Date) f.get(storable)));
					break;
				case Schema.FIELD_STORABLE:
					Storable s = (Storable) f.get(storable);
					if (s == null)
						buffer.putInt(0);
					else {
							buffer.putInt(s.getId());
					}
					break;

				default:
					throw new RuntimeException("Field type " + field
							+ " unsupported");

				}

			}
		}

		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return buffer.position();
	}

	private Date milisToDate(final long millis) {
		if (millis == -1)
			return null;
		return new Date(millis);
	}

	private long dateToMilis(final Date date) {
		if (date == null)
			return -1;
		return date.getTime();
	}

}
