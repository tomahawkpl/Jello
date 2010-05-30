package com.atteo.jello.schema;

public class Schema {
	public static final int FIELD_UNSUPPORTED = -1;
	public static final int FIELD_INT = 0;
	public static final int FIELD_STRING = 1;
	
	public int version;
	public int fields[];
	public String names[];
	
	public boolean equals(Schema schema) {
		int l = fields.length;
		int otherFields[] = schema.fields;
		String otherNames[] = schema.names;
		
		if (l != otherFields.length)
			return false;
		
		for (int i=0;i<l;i++) {
			if (names[i] != otherNames[i] || names[i].equals(otherNames[i]))
				return false;
		}
		
		return true;
	}
	
	public static int getFieldType(Class<?> type) {
		if (type == Integer.TYPE)
			return FIELD_INT;
		
		if (type == String.class)
			return FIELD_STRING;
		
		
		return FIELD_UNSUPPORTED;
	}
}