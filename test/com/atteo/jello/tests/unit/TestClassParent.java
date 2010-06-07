package com.atteo.jello.tests.unit;

import java.util.Date;

import com.atteo.jello.Storable;
import com.atteo.jello.associations.DatabaseField;

public class TestClassParent extends Storable {
	public @DatabaseField
	int fieldInt;
	public @DatabaseField
	short fieldShort;
	public @DatabaseField
	long fieldLong;
	public @DatabaseField
	byte fieldByte;
	public @DatabaseField
	char fieldChar;
	public @DatabaseField
	float fieldFloat;
	public @DatabaseField
	double fieldDouble;
	public @DatabaseField
	boolean fieldBoolean;
	public @DatabaseField
	Date fieldDate;
	public @DatabaseField
	String fieldString;
	
	public TestClassParent() {
		
	}
	
	public TestClassParent(int id) {
		super(id);
	}
}
