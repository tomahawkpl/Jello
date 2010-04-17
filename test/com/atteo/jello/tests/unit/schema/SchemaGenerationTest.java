package com.atteo.jello.tests.unit.schema;

import com.atteo.jello.tests.JelloTestCase;
import com.google.inject.Binder;

public class SchemaGenerationTest extends JelloTestCase {
	private TestClass storable;
	
	public void configure(Binder arg0) {
		
	}
	
	public void setUp() {
		super.setUp();
	}
	
	public void testGetSchema() {
		storable = new TestClass();
		
		
	}

}
