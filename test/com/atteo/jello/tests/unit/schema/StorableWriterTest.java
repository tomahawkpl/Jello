package com.atteo.jello.tests.unit.schema;

import java.util.Date;

import com.atteo.jello.PageUsage;
import com.atteo.jello.Record;
import com.atteo.jello.Storable;
import com.atteo.jello.schema.StorableWriter;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.atteo.jello.tests.unit.TestClass;
import com.atteo.jello.tests.unit.TestClassParent;
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
		binder.bind(Short.class)
		.annotatedWith(Names.named("pageSize")).toInstance(
				(short) 4096);
	}

	@Override
	public void setUp() {
		super.setUp();
	}

	public void testWriteRead() {
		final TestClass tc = new TestClass();
		tc.fieldInt = 1;
		tc.fieldShort = 2;
		tc.fieldLong = 3;
		tc.fieldByte = 4;
		tc.fieldBoolean = true;
		tc.fieldFloat = (float)5.26;
		tc.fieldDouble = (double)5;
		tc.fieldChar = 'T';
		tc.fieldDate = new Date();
		tc.fieldString = "test1";
		tc.fieldBelongs = new TestClassParent(7);

		final byte data[] = new byte[16384];

		writer.writeStorable(data, tc, tc.getSchema());

		final TestClass tc2 = new TestClass();
		writer.readStorable(data, tc2, tc2.getSchema());

		assertEquals(tc.fieldInt, tc2.fieldInt);
		assertEquals(tc.fieldShort, tc2.fieldShort);
		assertEquals(tc.fieldString, tc2.fieldString);
		assertEquals(tc.fieldLong, tc2.fieldLong);
		assertEquals(tc.fieldFloat, tc2.fieldFloat);
		assertEquals(tc.fieldDouble, tc2.fieldDouble);
		assertEquals(tc.fieldChar, tc2.fieldChar);
		assertEquals(tc.fieldBoolean, tc2.fieldBoolean);
		assertEquals(tc.fieldByte, tc2.fieldByte);
		assertEquals(tc.fieldDate.getTime(), tc2.fieldDate.getTime());
		assertEquals(tc.fieldBelongs.getId(), tc2.fieldBelongs.getId());
	}

	@Override
	protected Class<StorableWriter> interfaceUnderTest() {
		return StorableWriter.class;
	}

}
