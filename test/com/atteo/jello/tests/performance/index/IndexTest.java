package com.atteo.jello.tests.performance.index;

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
	private final short bTreeLeafCapacity = 4092;
	private final short bTreeNodeCapacity = 4092;

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
		int TESTSIZE = 2000;
		Record record = recordPool.acquire();
		
		record.setChunkUsed(100, (short)2, (short)4, true);
		
		startPerformanceTest(true);
		
		for (int i=0;i<TESTSIZE;i++) {
			record.setId(i);
			record.setSchemaVersion(i);
			index.insert(record);
		}
		
		endPerformanceTest();
	}
	
	protected void setUp() {
		super.setUp();
		
		index = indexFactory.create(klassIndexPageId);
	}

}
