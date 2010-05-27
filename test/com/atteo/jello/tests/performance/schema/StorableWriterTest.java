package com.atteo.jello.tests.performance.schema;

import com.atteo.jello.Storable;
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

	public void configure(Binder inder) {
		// TODO Auto-generated method stub

	}
	
	public void setUp() {
		super.setUp();
		
	}
	
	public void testTinyWrite() {
		Storable s = new TestClassTiny();
		
		byte data[] = new byte[16384];
		
		startPerformanceTest(true);
		
		for (int i=0;i<10;i++)
			writer.writeStorable(data, s, s.getSchema());
		
		endPerformanceTest();
	}
	
	public void testMediumWrite() {
		Storable s = new TestClassMedium();
		
		byte data[] = new byte[16384];
		
		startPerformanceTest(true);
		
		for (int i=0;i<10;i++)
			writer.writeStorable(data, s, s.getSchema());
		
		endPerformanceTest();
	}

}
