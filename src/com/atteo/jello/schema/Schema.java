package com.atteo.jello.schema;

import java.lang.reflect.Field;

public class Schema {
	public int version;
	public int hash;
	public Field[] fields;
	
	public boolean equals(Schema schema) {
		int l = fields.length;
		Field[] other = schema.fields;
		
		
		if (l != other.length)
			return false;
		
		for (int i=0;i<l;i++) {
			if (fields[i].getName() != other[i].getName() ||
					fields[i].getType() != other[i].getType())
				return false;
		}
		
		return true;
	}
}