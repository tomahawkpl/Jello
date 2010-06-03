package com.atteo.jello.tests.unit.schema;

import com.atteo.jello.PageUsage;
import com.atteo.jello.Record;
import com.atteo.jello.Storable;
import com.atteo.jello.schema.StorableWriter;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.atteo.jello.tests.unit.transaction.TransactionManagerMock;
import com.atteo.jello.transaction.TransactionManager;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.name.Names;

public abstract class StorableWriterTest extends
		JelloInterfaceTestCase<StorableWriter> {

	@Inject
	StorableWriter writer;

	public void configure(final Binder binder) {
		binder.requestStaticInjection(Record.class);
		binder.requestStaticInjection(PageUsage.class);
		binder.requestStaticInjection(Storable.class);
		binder.bind(TransactionManager.class).to(TransactionManagerMock.class);

		binder.bind(Integer.class).annotatedWith(Names.named("maxRecordPages"))
				.toInstance(4);
		binder.bind(Short.class)
				.annotatedWith(Names.named("freeSpaceInfoSize")).toInstance(
						(short) 4);
	}

	@Override
	public void setUp() {
		super.setUp();
	}

	public void testWriteRead() {
		final TestClass tc = new TestClass();
		tc.field1 = 1;
		tc.field2 = -1;
		tc.field3 = "test1";
		tc.field4 = "testfield2";

		final byte data[] = new byte[16384];

		writer.writeStorable(data, tc, tc.getSchema());

		final TestClass tc2 = new TestClass();
		writer.readStorable(data, tc2, tc2.getSchema());

		assertEquals(tc.field1, tc2.field1);
		assertEquals(tc.field2, tc2.field2);
		assertEquals(tc.field3, tc2.field3);
		assertEquals(tc.field4, tc2.field4);
	}

	@Override
	protected Class<StorableWriter> interfaceUnderTest() {
		return StorableWriter.class;
	}

}
