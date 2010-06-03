package com.atteo.jello.tests.unit.index;

import java.util.ArrayList;
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
	private final short bTreeLeafCapacity = 4096;
	private final short bTreeNodeCapacity = 4096;

	private final short pageSize = 4096;
	private final short blockSize = 128;
	private final short freeSpaceInfosPerPage = 1023;
	private final short freeSpaceInfoPageCapacity = 4092;
	private final short freeSpaceInfoSize = 4;
	private final int freeSpaceInfoPageId = 1;
	private final int maxRecordPages = 4;
	private final int maxRecordSize = maxRecordPages * pageSize;
	private final int nextFitHistogramClasses = 8;
	protected int klassIndexPageId;
	// --------------

	private @Inject
	IndexFactory indexFactory;
	private Index index;
	private @Inject
	Pool<Record> recordPool;

	@Inject
	private PagedFile pagedFile;
	@Inject
	private SpaceManagerPolicy spaceManagerPolicy;

	public void configure(final Binder binder) {
		binder.requestStaticInjection(Record.class);
		binder.requestStaticInjection(Page.class);
		binder.requestStaticInjection(PageUsage.class);

		binder.bind(PagedFile.class).to(PagedFileMock.class);
		binder.bind(SpaceManagerPolicy.class).to(Hybrid.class);

		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("nextFitHistogramClasses", String
				.valueOf(nextFitHistogramClasses));
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

	public void testCommit() {
		final int TESTSIZE = 2000;
		final Record record = recordPool.acquire();
		record.setChunkUsed(100, (short) 2, (short) 4, true);
		for (int i = 0; i < TESTSIZE; i++) {
			record.setId(i);
			record.setSchemaVersion(i);
			index.insert(record);
		}

		index.commit();

		final Index newIndex = indexFactory.create(klassIndexPageId);

		newIndex.load();

		// ((BTree) newIndex).debug();

		Record read = recordPool.acquire();

		for (int i = 0; i < TESTSIZE; i++) {
			read.clearUsage();
			read.setId(i);
			assertTrue(newIndex.find(read));
			assertEquals(i, read.getId());
			assertEquals(1, read.getPagesUsed());
			assertEquals(i, read.getSchemaVersion());
		}

		record.setId(TESTSIZE);
		assertFalse(newIndex.find(record));

		for (int i = 0; i < TESTSIZE / 2; i++)
			newIndex.remove(i);

		newIndex.commit();

		final Index index = indexFactory.create(klassIndexPageId);

		index.load();

		read = recordPool.acquire();

		for (int i = TESTSIZE / 2; i < TESTSIZE; i++) {
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

	public void testCreate() {
		index.create();
		index.load();
		final Record record = recordPool.acquire();
		record.setId(0);
		assertFalse(index.find(record));
	}

	public void testDelete() {
		final int TESTSIZE = 2000;
		final Record record = recordPool.acquire();
		record.setChunkUsed(99, (short) 2, (short) 4, true);
		for (int i = 0; i < TESTSIZE; i++) {
			record.setId(i);
			record.setSchemaVersion(i);
			index.insert(record);
		}

		// ((BTree)index).debug();

		final Record read = recordPool.acquire();

		final ArrayList<Integer> ids = new ArrayList<Integer>();

		for (int i = 0; i < TESTSIZE; i++) {
			read.clearUsage();
			read.setId(i);
			assertTrue(index.find(read));
			assertEquals(i, read.getId());
			assertEquals(1, read.getPagesUsed());
			assertEquals(i, read.getSchemaVersion());
			ids.add(i);
		}

		for (int i = 0; i < TESTSIZE; i++) {
			final int r = (int) (Math.random() * ids.size());
			index.remove(ids.get(r));
			// ((BTree)index).debug();
			read.setId(ids.get(r));
			assertFalse(index.find(read));
			ids.remove(r);
		}

		for (int i = 0; i < TESTSIZE; i++) {
			read.setId(i);
			assertFalse(index.find(read));
		}

		for (int i = 0; i < TESTSIZE; i++) {
			record.setId(i);
			record.setSchemaVersion(i);
			index.insert(record);
		}

		for (int i = 0; i < TESTSIZE; i++) {
			read.setId(i);
			assertTrue(index.find(read));
		}

		record.setId(TESTSIZE);
		assertFalse(index.find(record));
	}

	public void testDelete2() {
		final int TESTSIZE = 2000;

		final ArrayList<Integer> ids = new ArrayList<Integer>();

		final Record record = recordPool.acquire();
		record.setChunkUsed(99, (short) 2, (short) 4, true);
		for (int i = 0; i < TESTSIZE / 2; i++) {
			record.setId(i);
			record.setSchemaVersion(i);
			index.insert(record);
			ids.add(i);
		}

		// ((BTree)index).debug();

		final Record read = recordPool.acquire();

		for (int i = TESTSIZE / 2; i < TESTSIZE; i++) {
			final int r = (int) (Math.random() * ids.size());
			// ((BTree)index).debug();

			index.remove(ids.get(r));
			read.setId(ids.get(r));
			assertFalse(index.find(read));
			ids.remove(r);

			record.setId(i);
			record.setSchemaVersion(i);
			index.insert(record);
			ids.add(i);
			// ((BTree)index).debug();
			assertTrue(index.find(record));
		}

		record.setId(TESTSIZE);
		assertFalse(index.find(record));
	}

	public void testExtensive() {
		final int TESTSIZE = 2000;

		final ArrayList<Integer> ids = new ArrayList<Integer>();

		final Record record = recordPool.acquire();
		for (int i = 0; i < TESTSIZE / 2; i++) {
			// insert
			record.clearUsage();
			record.setId(i);
			record.setSchemaVersion(i);
			if (i % 2 == 1) {
				record.setChunkUsed(99, (short) 2, (short) 4, true);
				record.setChunkUsed(100, (short) 2, (short) 4, true);
			} else
				record.setChunkUsed(99, (short) 2, (short) 4, true);
			index.insert(record);
			ids.add(i);
			assertTrue(index.find(record));
			assertEquals(i, record.getId());
			assertEquals(i % 2 + 1, record.getPagesUsed());
			assertEquals(i, record.getSchemaVersion());
		}

		for (int i = TESTSIZE / 2; i < TESTSIZE; i++) {
			// insert
			record.clearUsage();
			record.setId(i);
			record.setSchemaVersion(i);
			if (i % 2 == 1) {
				record.setChunkUsed(99, (short) 2, (short) 4, true);
				record.setChunkUsed(100, (short) 2, (short) 4, true);
			} else
				record.setChunkUsed(99, (short) 2, (short) 4, true);
			index.insert(record);
			ids.add(i);
			assertTrue(index.find(record));
			assertEquals(i, record.getId());
			assertEquals(i % 2 + 1, record.getPagesUsed());
			assertEquals(i, record.getSchemaVersion());

			// delete
			int r = (int) (Math.random() * ids.size());
			index.remove(ids.get(r));
			record.setId(ids.get(r));
			assertFalse(index.find(record));
			ids.remove(r);

			// update
			r = (int) (Math.random() * ids.size());
			final int id = ids.get(r);
			record.setId(id);
			index.find(record);
			int t;
			if (record.getPagesUsed() == 1) {
				t = 2;
				record.clearUsage();
				record.setChunkUsed(99, (short) 2, (short) 4, true);
				record.setChunkUsed(100, (short) 2, (short) 4, true);
			} else {
				t = 1;
				record.clearUsage();
				record.setChunkUsed(99, (short) 2, (short) 4, true);
			}

			index.update(record);

			assertTrue(index.find(record));
			assertEquals(id, record.getId());
			assertEquals(t, record.getPagesUsed());
			assertEquals(id, record.getSchemaVersion());
		}

	}

	public void testInsert() {
		final int TESTSIZE = 3000;
		final Record record = recordPool.acquire();
		record.setChunkUsed(100, (short) 2, (short) 4, true);
		for (int i = 0; i < TESTSIZE; i++) {
			record.setId(i);
			record.setSchemaVersion(i);
			index.insert(record);
		}

		final Record read = recordPool.acquire();

		for (int i = 0; i < TESTSIZE; i++) {
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

	public void testUpdate() {
		final int TESTSIZE = 2000;

		final ArrayList<Integer> ids = new ArrayList<Integer>();

		final Record record = recordPool.acquire();
		record.setChunkUsed(99, (short) 2, (short) 4, true);
		for (int i = 0; i < TESTSIZE; i++) {
			record.setId(i);
			record.setSchemaVersion(i);
			index.insert(record);
			ids.add(i);
		}

		record.setChunkUsed(100, (short) 12, (short) 15, true);

		for (int i = 0; i < TESTSIZE; i++) {
			record.setId(i);
			record.setSchemaVersion(i);
			index.update(record);
			// ((BTree)index).debug();
		}

		for (int i = 0; i < TESTSIZE; i++) {
			record.setId(i);
			assertTrue(index.find(record));
			assertEquals(i, record.getId());
			assertEquals(2, record.getPagesUsed());
			assertEquals(i, record.getSchemaVersion());
		}
	}

	public void testUpdate2() {
		final int TESTSIZE = 2000;

		final ArrayList<Integer> ids = new ArrayList<Integer>();

		final Record record = recordPool.acquire();
		record.setChunkUsed(99, (short) 2, (short) 4, true);
		record.setChunkUsed(100, (short) 2, (short) 4, true);
		record.setChunkUsed(101, (short) 2, (short) 4, true);
		for (int i = 0; i < TESTSIZE; i++) {
			record.setId(i);
			record.setSchemaVersion(i);
			index.insert(record);
			ids.add(i);
		}

		record.clearUsage();
		record.setChunkUsed(100, (short) 12, (short) 15, true);

		for (int i = 0; i < TESTSIZE; i++) {
			record.setId(i);
			record.setSchemaVersion(i);
			index.update(record);
			// ((BTree)index).debug();
		}

		for (int i = 0; i < TESTSIZE; i++) {
			record.setId(i);
			assertTrue(index.find(record));
			assertEquals(i, record.getId());
			assertEquals(1, record.getPagesUsed());
			assertEquals(i, record.getSchemaVersion());
		}
	}

	@Override
	protected void bindImplementation(final Binder binder) {
		binder.bind(IndexFactory.class).toProvider(
				FactoryProvider
						.newFactory(IndexFactory.class, implementation()));
	}

	@Override
	protected Class<Index> interfaceUnderTest() {
		return Index.class;
	}

	@Override
	protected void setUp() {
		super.setUp();

		if (pagedFile.exists())
			pagedFile.remove();

		pagedFile.create();
		pagedFile.open();
		pagedFile.addPages(10);

		spaceManagerPolicy.create();

		klassIndexPageId = spaceManagerPolicy.acquirePage();
		index = indexFactory.create(klassIndexPageId);
	}

}
