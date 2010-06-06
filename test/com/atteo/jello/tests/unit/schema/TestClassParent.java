package com.atteo.jello.tests.unit.schema;

import com.atteo.jello.Storable;
import com.atteo.jello.associations.DatabaseField;

public class TestClassParent extends Storable {
	@DatabaseField String name;
	
	public TestClassParent() {
		
	}
	
	public TestClassParent(int id) {
		super(id);
	}
}
