package com.atteo.jello.tests.performance.schema;

import com.atteo.jello.Storable;
import com.atteo.jello.schema.StorableWriter;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.google.inject.Binder;
import com.google.inject.Inject;

public abstract class StorableWriterTest extends
		JelloInterfaceTestCase<StorableWriter> {

	@Inject
	StorableWriter writer;

	public void configure(final Binder inder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUp() {
		super.setUp();

	}

	public void testMediumWrite() {
		final Storable s = new TestClassMedium();

		final byte data[] = new byte[16384];

		startPerformanceTest(true);

		for (int i = 0; i < 10; i++)
			writer.writeStorable(data, s, s.getSchema());

		endPerformanceTest();
	}

	public void testTinyWrite() {
		final Storable s = new TestClassTiny();

		final byte data[] = new byte[16384];

		startPerformanceTest(true);

		for (int i = 0; i < 10; i++)
			writer.writeStorable(data, s, s.getSchema());

		endPerformanceTest();
	}

	@Override
	protected Class<StorableWriter> interfaceUnderTest() {
		return StorableWriter.class;
	}

}
