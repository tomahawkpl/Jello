package com.atteo.jello.tests.unit.space;

import java.nio.ByteBuffer;
import java.util.HashMap;

import com.atteo.jello.space.SpaceManager;
import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.atteo.jello.tests.unit.store.PagedFileMock;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.name.Names;

public abstract class SpaceManagerTest extends
		JelloInterfaceTestCase<SpaceManager> {
	// ---- SETTINGS
	private final short pageSize = 4096;
	private final short blockSize = 128;
	private final short freeSpaceInfosPerPage = 1023;
	private final short freeSpaceInfoPageCapacity = 4092;
	private final short freeSpaceInfoSize = 4;
	private final int freeSpaceMapPageId = 1;

	// --------------

	@Inject
	private SpaceManager spaceManager;
	
	@Inject
	private PagedFile pagedFile;

	@Override
	protected Class<SpaceManager> interfaceUnderTest() {
		return SpaceManager.class;
	}

	public void configure(final Binder binder) {
		binder.bind(PagedFile.class).to(PagedFileMock.class);

		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("blockSize", String.valueOf(blockSize));
		p.put("pageSize", String.valueOf(pageSize));
		p.put("freeSpaceInfoSize", String.valueOf(freeSpaceInfoSize));
		p.put("freeSpaceInfoPageCapacity", String
				.valueOf(freeSpaceInfoPageCapacity));
		p.put("freeSpaceInfosPerPage", String.valueOf(freeSpaceInfosPerPage));
		p.put("freeSpaceMapPageId", String.valueOf(freeSpaceMapPageId));

		
		Names.bindProperties(binder, p);

	}

	public void testCreate() {
		final Page p = new Page();

		p.setId(freeSpaceMapPageId);
		pagedFile.readPage(p);

		final ByteBuffer b = ByteBuffer.wrap(p.getData());

		assertEquals(-1, b.getLong(0));
	}

	public void testFreeSpaceOnPage() {
		assertEquals(4096, spaceManager.freeSpaceOnPage(0));
		for (short i = 0; i < 32; i++) {
			spaceManager.setBlockUsed(0, i, true);
			assertEquals(4096 - 128 * (i + 1), spaceManager.freeSpaceOnPage(0));
		}
		assertEquals(0, spaceManager.freeSpaceOnPage(freeSpaceMapPageId));

	}

	public void testIsBlockUsed() {
		for (int i = 0; i < 32; i++)
			assertFalse(spaceManager.isBlockUsed(0, (short) i));

		assertTrue(spaceManager.isPageUsed(1));

		for (int i = 0; i < 32; i++)
			assertTrue(spaceManager.isBlockUsed(1, (short) i));

	}

	public void testIsPageUsed() {
		assertFalse(spaceManager.isPageUsed(0));
		assertTrue(spaceManager.isPageUsed(1));
		assertFalse(spaceManager.isPageUsed(2));
		assertFalse(spaceManager.isPageUsed(3));
		assertFalse(spaceManager.isPageUsed(4));

		final Page p = new Page();

		p.setId(freeSpaceMapPageId);
		pagedFile.readPage(p);

		final ByteBuffer b = ByteBuffer.wrap(p.getData());

		b.position(8);

		for (int i = 0; i < 5; i++)
			assertEquals(0, b.getInt());

	}

	public void testSetBlockUsed() {
		spaceManager.setBlockUsed(0, (short) 30, true);
		assertTrue(spaceManager.isBlockUsed(0, (short) 30));
		assertTrue(spaceManager.isPageUsed(0));

		spaceManager.setBlockUsed(0, (short) 30, false);
		assertFalse(spaceManager.isBlockUsed(0, (short) 30));

	}

	public void testSetPageUsed() {
		assertFalse(spaceManager.isPageUsed(0));

		spaceManager.setPageUsed(2, true);

		assertFalse(spaceManager.isPageUsed(0));
		assertTrue(spaceManager.isPageUsed(1));
		assertTrue(spaceManager.isPageUsed(2));
		assertFalse(spaceManager.isPageUsed(3));
		assertFalse(spaceManager.isPageUsed(4));

	}

	public void testTotalFreeSpace() {
		assertEquals(pageSize * 4, spaceManager.totalFreeSpace());
		for (short i = 0; i < 32; i++) {
			spaceManager.setBlockUsed(0, i, true);
			assertEquals(4 * pageSize - blockSize * (i + 1), spaceManager
					.totalFreeSpace());
		}
	}

	public void testUpdate() {
		pagedFile.addPages(1022);
		spaceManager.update();
		assertFalse(spaceManager.isPageUsed(1022));
		spaceManager.setPageUsed(1022, true);
		assertTrue(spaceManager.isPageUsed(1022));

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
