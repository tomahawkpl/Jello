package com.atteo.jello.tests.unit.schema;

import java.util.Date;

import com.atteo.jello.Storable;
import com.atteo.jello.associations.BelongsTo;
import com.atteo.jello.associations.DatabaseField;

public class TestClass extends Storable {
	@DatabaseField
	int fieldInt;
	@DatabaseField
	short fieldShort;
	@DatabaseField
	long fieldLong;
	@DatabaseField
	byte fieldByte;
	@DatabaseField
	char fieldChar;
	@DatabaseField
	float fieldFloat;
	@DatabaseField
	double fieldDouble;
	@DatabaseField
	boolean fieldBoolean;
	@DatabaseField
	Date fieldDate;
	@DatabaseField
	String fieldString;
	@BelongsTo
	TestClassParent fieldBelongs = null;
}
