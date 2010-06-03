package com.atteo.jello.tests.unit.schema;

import com.atteo.jello.DatabaseField;
import com.atteo.jello.Storable;

public class TestClass extends Storable {
	@DatabaseField
	int field1;
	@DatabaseField
	int field2;
	@DatabaseField
	String field3;
	@DatabaseField
	String field4;
}
