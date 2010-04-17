package com.atteo.jello.tests.unit.schema;

import com.atteo.jello.schema.StorableWriter;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.google.inject.Binder;
import com.google.inject.Inject;

public abstract class StorableWriterTest extends JelloInterfaceTestCase<StorableWriter> {

	@Inject StorableWriter writer;
	
	@Override
	protected Class<StorableWriter> interfaceUnderTest() {
		return StorableWriter.class;
	}

	public void setUp() {
		super.setUp();
	}
	
	public void testWriteRead() {
		TestClass tc = new TestClass();
		tc.field1 = 1;
		tc.field2 = -1;
		tc.field3 = "test1";
		tc.field4 = "testfield2";
		
		byte[] data = writer.writeStorable(tc, tc.getSchema());
		
		TestClass tc2 = new TestClass();
		writer.readStorable(data, tc2, tc.getSchema());
		
		assertEquals(tc.field1, tc2.field1);
		assertEquals(tc.field2, tc2.field2);
		assertEquals(tc.field3, tc2.field3);
		assertEquals(tc.field4, tc2.field4);
	}
	
	public void configure(Binder binder) {
		
	}

}
