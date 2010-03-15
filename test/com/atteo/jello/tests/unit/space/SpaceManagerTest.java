package com.atteo.jello.tests.unit.space;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.test.InstrumentationTestCase;

import com.atteo.jello.space.SpaceManagerNative;
import com.atteo.jello.store.DatabaseFile;
import com.atteo.jello.store.ListPage;
import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.tests.unit.store.PagedFileMock;

public class SpaceManagerTest extends InstrumentationTestCase {
	private SpaceManagerNative spaceManager;
	private PagedFile pagedFile;

	@Override
	protected void setUp() throws IOException {
		pagedFile = new PagedFileMock((short) 4096);
		pagedFile.addPages(5);

		spaceManager = new SpaceManagerNative(pagedFile, (short) 4, (short) 32,
				(short) 1022, new ListPage((short) 4096), (short) 128);
		spaceManager.create();
	}

	public void testCreate() {
		Page p = new Page((short) 4096);

		p.setId(DatabaseFile.PAGE_FREE_SPACE_MAP);
		pagedFile.readPage(p);

		ByteBuffer b = ByteBuffer.wrap(p.getData());

		assertEquals(-1, b.getLong(0));
	}

	public void testIsPageUsed() {
		assertFalse(spaceManager.isPageUsed(0));
		assertTrue(spaceManager.isPageUsed(1));
		assertFalse(spaceManager.isPageUsed(2));
		assertFalse(spaceManager.isPageUsed(3));
		assertFalse(spaceManager.isPageUsed(4));

		Page p = new Page((short) 4096);

		p.setId(DatabaseFile.PAGE_FREE_SPACE_MAP);
		pagedFile.readPage(p);

		ByteBuffer b = ByteBuffer.wrap(p.getData());

		b.position(8);

		for (int i = 0; i < 5; i++)
			assertEquals(0, b.getInt());

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

	public void testIsBlockUsed() {
		for (int i = 0; i < 32; i++)
			assertFalse(spaceManager.isBlockUsed(0, (short) i));

		assertTrue(spaceManager.isPageUsed(1));

		for (int i = 0; i < 32; i++)
			assertTrue(spaceManager.isBlockUsed(1, (short) i));

	}

	public void testSetBlockUsed() {
		spaceManager.setBlockUsed(0, (short) 30, true);
		assertTrue(spaceManager.isBlockUsed(0, (short) 30));
		assertTrue(spaceManager.isPageUsed(0));

		spaceManager.setBlockUsed(0, (short) 30, false);
		assertFalse(spaceManager.isBlockUsed(0, (short) 30));

	}

	public void testFreeSpaceOnPage() {
		assertEquals(4096, spaceManager.freeSpaceOnPage(0));
		for (short i=0;i<32;i++) {
			spaceManager.setBlockUsed(0, i, true);
			assertEquals(4096 - 128*(i+1), spaceManager.freeSpaceOnPage(0));
		}
		assertEquals(0, spaceManager.freeSpaceOnPage(DatabaseFile.PAGE_FREE_SPACE_MAP));

	}
	
	public void testTotalFreeSpace() {
		assertEquals(4096 * 4, spaceManager.totalFreeSpace());
		for (short i=0;i<32;i++) {
			spaceManager.setBlockUsed(0, i, true);
			assertEquals(4*4096 - 128*(i+1), spaceManager.totalFreeSpace());
		}
	}
	
	@Override
	protected void tearDown() {
	}
}
