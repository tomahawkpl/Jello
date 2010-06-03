package com.atteo.jello.tests.unit.space;

import com.atteo.jello.PageUsage;
import com.atteo.jello.Record;
import com.atteo.jello.tests.JelloTestCase;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.name.Names;

public class RecordTest extends JelloTestCase {
	@Inject
	Record record;

	public void configure(final Binder binder) {
		binder.requestStaticInjection(Record.class);
		binder.requestStaticInjection(PageUsage.class);

		binder.bind(Integer.class).annotatedWith(Names.named("maxRecordPages"))
				.toInstance(4);
		binder.bind(Short.class)
				.annotatedWith(Names.named("freeSpaceInfoSize")).toInstance(
						(short) 4);
	}

	public void testGetPagesUsed() {
		assertEquals(0, record.getPagesUsed());
		record.setChunkUsed(0, (short) 0, (short) 5, false);
		assertEquals(0, record.getPagesUsed());
		record.setChunkUsed(0, (short) 0, (short) 5, true);
		assertEquals(1, record.getPagesUsed());
		record.setChunkUsed(0, (short) 5, (short) 6, true);
		assertEquals(1, record.getPagesUsed());
		record.setChunkUsed(1, (short) 0, (short) 5, true);
		assertEquals(2, record.getPagesUsed());
		record.setChunkUsed(0, (short) 0, (short) 5, false);
		assertEquals(2, record.getPagesUsed());
		record.setChunkUsed(0, (short) 5, (short) 6, false);
		assertEquals(1, record.getPagesUsed());
		record.setChunkUsed(1, (short) 0, (short) 5, false);
		assertEquals(0, record.getPagesUsed());

	}

	@Override
	protected void setUp() {
		super.setUp();
	}

}
