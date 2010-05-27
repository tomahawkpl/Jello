package com.atteo.jello.tests.unit.index;

import java.util.HashMap;

import android.util.Pool;

import com.atteo.jello.PageUsage;
import com.atteo.jello.Record;
import com.atteo.jello.index.Index;
import com.atteo.jello.index.IndexFactory;
import com.atteo.jello.space.Hybrid;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.atteo.jello.tests.unit.store.PagedFileMock;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Names;

public abstract class IndexTest extends JelloInterfaceTestCase<Index> {

	// ---- SETTINGS
	private final short bTreeLeafCapacity = 32;
	private final short bTreeNodeCapacity = 16;

	private final short pageSize = 4096;
	private final short blockSize = 128;
	private final short freeSpaceInfosPerPage = 1023;
	private final short freeSpaceInfoPageCapacity = 4092;
	private final short freeSpaceInfoSize = 4;
	private final int freeSpaceInfoPageId = 1;
	private final int maxRecordPages = 4;
	private final int maxRecordSize = maxRecordPages * pageSize;
	private final int nextFitHistogramClasses = 8;
	protected final int klassIndexPageId = 3;
	// --------------
	
	private @Inject IndexFactory indexFactory;
	private Index index;
	private @Inject Pool<Record> recordPool;
	
	@Override
	protected Class<Index> interfaceUnderTest() {
		return Index.class;
	}

	@Override
	protected void bindImplementation(Binder binder) {
		binder.bind(IndexFactory.class).toProvider(
			    FactoryProvider.newFactory(IndexFactory.class, implementation()));
	}
	
	public void configure(Binder binder) {
		binder.requestStaticInjection(Record.class);
		binder.requestStaticInjection(Page.class);
		binder.requestStaticInjection(PageUsage.class);

		
		binder.bind(PagedFile.class).to(PagedFileMock.class);
		binder.bind(SpaceManagerPolicy.class).to(Hybrid.class);
		
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("nextFitHistogramClasses", String.valueOf(nextFitHistogramClasses));
		p.put("bTreeNodeCapacity", String.valueOf(bTreeNodeCapacity));
		p.put("bTreeLeafCapacity", String.valueOf(bTreeLeafCapacity));
		
		p.put("blockSize", String.valueOf(blockSize));
		p.put("pageSize", String.valueOf(pageSize));
		p.put("freeSpaceInfoSize", String.valueOf(freeSpaceInfoSize));
		p.put("freeSpaceInfoPageCapacity", String
				.valueOf(freeSpaceInfoPageCapacity));
		p.put("freeSpaceInfosPerPage", String.valueOf(freeSpaceInfosPerPage));
		p.put("freeSpaceInfoPageId", String.valueOf(freeSpaceInfoPageId));
		p.put("maxRecordSize", String.valueOf(maxRecordSize));
		p.put("maxRecordPages", String.valueOf(maxRecordPages));
		Names.bindProperties(binder, p);
	}
	
	public void testInsert() {
		int TESTSIZE = 100;
		Record record = recordPool.acquire();
		record.setChunkUsed(100, (short)2, (short)4, true);
		for (int i=0;i<TESTSIZE;i++) {
			record.setId(i);
			record.setSchemaVersion(i);
			index.insert(record);
		}
		
		Record read = recordPool.acquire();
//		index.find(read);
		
		for (int i=0;i<TESTSIZE;i++) {
			read.clearUsage();
			read.setId(i);
			assertTrue(index.find(read));
			assertEquals(i, read.getId());
			assertEquals(1, read.getPagesUsed());
			assertEquals(i, read.getSchemaVersion());
		}
		
		record.setId(TESTSIZE);
		assertFalse(index.find(record));
	
	}
	
	public void testDelete() {
		Record record = recordPool.acquire();
		record.setId(5);
		record.setChunkUsed(100, (short)2, (short)4, true);
		index.insert(record);
		assertTrue(index.find(record));
		index.remove(record.getId());
		assertFalse(index.find(record));
	}
	
	public void testUpdate() {
		Record record = recordPool.acquire();
		record.setId(5);
		record.setSchemaVersion(11);
		record.setChunkUsed(100, (short)2, (short)4, true);
		
		Record record2 = recordPool.acquire();
		record2.setId(5);
		record2.setSchemaVersion(13);
		record2.setChunkUsed(100, (short)5, (short)7, true);
		record2.setChunkUsed(101, (short)5, (short)7, true);
		
		Record read = recordPool.acquire();
		read.setId(5);
		assertFalse(index.find(read));
		index.insert(record);
		
		assertTrue(index.find(read));
		index.update(record2);
		assertTrue(index.find(read));

		assertEquals(record2.getId(), read.getId());
		assertEquals(record2.getPagesUsed(), read.getPagesUsed());
		assertEquals(record2.getSchemaVersion(), read.getSchemaVersion());
	}
	
	protected void setUp() {
		super.setUp();
		
		index = indexFactory.create(klassIndexPageId);
	}

}
