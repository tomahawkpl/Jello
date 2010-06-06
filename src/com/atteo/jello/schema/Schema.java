package com.atteo.jello.schema;

import java.util.Date;

public class Schema {
	public static final int FIELD_UNSUPPORTED = -1;
	public static final int FIELD_INT = 0;
	public static final int FIELD_BYTE = 1;
	public static final int FIELD_STRING = 2;
	public static final int FIELD_SHORT = 3;
	public static final int FIELD_BOOLEAN = 4;
	public static final int FIELD_CHAR = 5;
	public static final int FIELD_FLOAT = 6;
	public static final int FIELD_DOUBLE = 7;
	public static final int FIELD_LONG = 8;
	public static final int FIELD_DATE = 9;
	public static final int FIELD_STORABLE = 10;

	public int version;
	public int fields[];
	public String names[];
	
	
	public static int getFieldType(final Class<?> type) {
		if (type.equals(Integer.TYPE))
			return FIELD_INT;
		if (type.equals(String.class))
			return FIELD_STRING;
		if (type.equals(Byte.TYPE))
			return FIELD_BYTE;
		if (type.equals(Boolean.TYPE))
			return FIELD_BOOLEAN;
		if (type.equals(Short.TYPE))
			return FIELD_SHORT;
		if (type.equals(Character.TYPE))
			return FIELD_CHAR;
		if (type.equals(Float.TYPE))
			return FIELD_FLOAT;
		if (type.equals(Double.TYPE))
			return FIELD_DOUBLE;
		if (type.equals(Long.TYPE))
			return FIELD_LONG;
		if (type.equals(Date.class))
			return FIELD_DATE;

		
		
		return FIELD_UNSUPPORTED;
	}

	public boolean equals(final Schema schema) {
		final int l = fields.length;
		final int otherFields[] = schema.fields;
		final String otherNames[] = schema.names;

		if (l != otherFields.length)
			return false;

		for (int i = 0; i < l; i++)
			if (fields[i] != otherFields[i] || !names[i].equals(otherNames[i]))
				return false;

		return true;
	}
}