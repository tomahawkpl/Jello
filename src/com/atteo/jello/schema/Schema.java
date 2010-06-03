package com.atteo.jello.schema;

public class Schema {
	public static final int FIELD_UNSUPPORTED = -1;
	public static final int FIELD_INT = 0;
	public static final int FIELD_STRING = 1;

	public int version;
	public int fields[];
	public String names[];

	public static int getFieldType(final Class<?> type) {
		if (type.equals(Integer.TYPE))
			return FIELD_INT;

		if (type.equals(String.class))
			return FIELD_STRING;

		return FIELD_UNSUPPORTED;
	}

	public boolean equals(final Schema schema) {
		final int l = fields.length;
		final int otherFields[] = schema.fields;
		final String otherNames[] = schema.names;

		if (l != otherFields.length)
			return false;

		for (int i = 0; i < l; i++)
			if (names[i] != otherNames[i] || names[i].equals(otherNames[i]))
				return false;

		return true;
	}
}