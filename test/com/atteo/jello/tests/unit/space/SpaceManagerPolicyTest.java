package com.atteo.jello.tests.unit.space;

import java.util.HashMap;

import com.atteo.jello.PageUsage;
import com.atteo.jello.Record;
import com.atteo.jello.space.AppendOnlyCache;
import com.atteo.jello.space.AppendOnlyCacheNative;
import com.atteo.jello.space.SpaceManager;
import com.atteo.jello.space.SpaceManagerNative;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.atteo.jello.tests.unit.store.PagedFileMock;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.name.Names;

public abstract class SpaceManagerPolicyTest extends
		JelloInterfaceTestCase<SpaceManagerPolicy> {
	// ---- SETTINGS
	private final short pageSize = 4096;
	private final short blockSize = 128;
	private final short freeSpaceInfosPerPage = 1023;
	private final short freeSpaceInfoPageCapacity = 4092;
	private final short freeSpaceInfoSize = 4;
	private final int freeSpaceMapPageId = 1;
	private final int appendOnlyCacheSize = 8;
	private final int maxRecordPages = 4;
	private final int maxRecordSize = maxRecordPages * pageSize;
	
	// --------------

	@Inject	private SpaceManager spaceManager;
	@Inject	private PagedFile pagedFile;
	@Inject	private SpaceManagerPolicy policy;
	
	@Override
	protected Class<SpaceManagerPolicy> interfaceUnderTest() {
		return SpaceManagerPolicy.class;
	}

	public void configure(final Binder binder) {
		binder.bind(PagedFile.class).to(PagedFileMock.class);
		binder.bind(SpaceManager.class).to(SpaceManagerNative.class);
		binder.bind(AppendOnlyCache.class).to(AppendOnlyCacheNative.class);
		
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("blockSize", String.valueOf(blockSize));
		p.put("pageSize", String.valueOf(pageSize));
		p.put("freeSpaceInfoSize", String.valueOf(freeSpaceInfoSize));
		p.put("freeSpaceInfoPageCapacity", String
				.valueOf(freeSpaceInfoPageCapacity));
		p.put("freeSpaceInfosPerPage", String.valueOf(freeSpaceInfosPerPage));
		p.put("freeSpaceMapPageId", String.valueOf(freeSpaceMapPageId));
		p.put("appendOnlyCacheSize", String.valueOf(appendOnlyCacheSize));
		p.put("maxRecordSize", String.valueOf(maxRecordSize));
		p.put("maxRecordPages", String.valueOf(maxRecordPages));

		
		Names.bindProperties(binder, p);

	}

	public void testAcquirePage() {
		assertEquals(5, policy.acquirePage());
		assertEquals(6,pagedFile.getPageCount());
		assertTrue(spaceManager.isPageUsed(5));
		assertEquals(6, policy.acquirePage());
		assertEquals(7,pagedFile.getPageCount());
		assertTrue(spaceManager.isPageUsed(6));
		assertEquals(7,pagedFile.getPageCount());
		assertFalse(spaceManager.isPageUsed(0));
		assertEquals(7, policy.acquirePage());
		assertEquals(8,pagedFile.getPageCount());
		assertTrue(spaceManager.isPageUsed(7));
	}
	
	public void testReleasePage() {
		assertEquals(5, policy.acquirePage());
		assertEquals(6, policy.acquirePage());
		assertEquals(7,pagedFile.getPageCount());
		assertTrue(spaceManager.isPageUsed(5));
		assertTrue(spaceManager.isPageUsed(6));
		policy.releasePage(5);
		policy.releasePage(6);
		assertEquals(2,pagedFile.getPageCount());
		assertFalse(spaceManager.isPageUsed(2));
		assertFalse(spaceManager.isPageUsed(5));
		assertFalse(spaceManager.isPageUsed(6));
		
	}

	public void testSimpleAcquireRecord() {
		Record r = policy.acquireRecord(512);
		assertEquals(512, recordSize(r));
		r = policy.acquireRecord(511);
		assertEquals(512, recordSize(r));
		r = policy.acquireRecord(4000);
		assertEquals(4096, recordSize(r));
		r = policy.acquireRecord(1024);
		assertEquals(1024, recordSize(r));
	}
	
	public void testAcquireReleaseRecord() {
		Record r1 = policy.acquireRecord(512);
		assertEquals(512, recordSize(r1));
		Record r2 = policy.acquireRecord(511);
		assertEquals(512, recordSize(r2));
		policy.releaseRecord(r1);
		r1 = policy.acquireRecord(4224);
		assertEquals(4224, recordSize(r1));
	}
	
	private int recordSize(Record r) {
		int result = 0;
		int pages = r.getPagesUsed();
		for (int i=0;i<pages;i++) {
			PageUsage u = r.getPageUsage(i);
			byte[] b = u.usage;
			for (int j=0;j<b.length;j++)
				for (int k=0;k<Byte.SIZE;k++)
					if ((b[j] & (1 << k)) != 0)
						result += blockSize;
				
		}
		return result;
	}
	
	@Override
	protected void setUp() {
		super.setUp();
		if (!pagedFile.exists())
			pagedFile.create();
		pagedFile.open();
		pagedFile.addPages(5);

		spaceManager.create();
	}

	@Override
	protected void tearDown() {
		pagedFile.close();
		pagedFile.remove();
	}
}
