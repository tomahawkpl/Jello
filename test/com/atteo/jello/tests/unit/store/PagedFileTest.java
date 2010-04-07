package com.atteo.jello.tests.unit.store;

import java.io.IOException;
import java.util.Arrays;

import android.util.Pool;

import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public abstract class PagedFileTest extends JelloInterfaceTestCase<PagedFile> {
	@Inject
	private PagedFile pagedFile;

	@Inject
	@Named("pageSize")
	private short pageSize;
	@Inject
	private Pool<Page> pagePool;

	@Override
	protected Class<PagedFile> interfaceUnderTest() {
		return PagedFile.class;
	}

	public void configure(Binder binder) {
		binder.bind(Short.class).annotatedWith(Names.named("pageSize"))
				.toInstance((short) 4096);
		String path = getInstrumentation().getContext().getDatabasePath(
				"testfile").getAbsolutePath();
		binder.bind(String.class).annotatedWith(Names.named("fullpath"))
				.toInstance(path);
	}

	public void testAddPages() throws IOException {
		final int TESTSIZE = 10;
		long id;

		for (int i = 0; i < TESTSIZE; i++) {
			id = pagedFile.addPages(1);
			assertEquals(i, id);
			assertEquals(i + 1, pagedFile.getPageCount());
			assertEquals(pageSize * (i + 1), pagedFile.getFileLength());
		}
	}

	public void testPagedFile() {
		assertTrue(!pagedFile.isReadOnly());
		assertEquals(0, pagedFile.getPageCount());
		assertEquals(0, pagedFile.getFileLength());
	}

	public void testReadPage() throws IOException {
		final Page p = pagePool.acquire();
		final byte[] saved = p.getData();
		pagedFile.addPages(1);
		p.setId(0);
		pagedFile.writePage(p);
		pagedFile.readPage(p);
		assertTrue(Arrays.equals(p.getData(), saved));
		pagePool.release(p);
	}

	public void testRemovePage() throws IOException {
		pagedFile.removePages(1);
		assertEquals(0, pagedFile.getFileLength());
		assertEquals(0, pagedFile.getPageCount());

		assertEquals(0, pagedFile.addPages(1));
		assertEquals(pageSize, pagedFile.getFileLength());
		assertEquals(1, pagedFile.getPageCount());

		pagedFile.removePages(1);
		assertEquals(0, pagedFile.getFileLength());
		assertEquals(0, pagedFile.getPageCount());

		pagedFile.removePages(1);
		assertEquals(0, pagedFile.getFileLength());
		assertEquals(0, pagedFile.getPageCount());
	}

	public void testWriteGetPage() {
		final int FILESIZE = 100;
		assertEquals(FILESIZE - 1, pagedFile.addPages(FILESIZE));

		final Page p = pagePool.acquire();

		for (int i = 0; i < FILESIZE; i++) {
			p.setId(i);
			p.getData()[i % pageSize] = (byte) (i % 255);
			pagedFile.writePage(p);
			p.getData()[i % pageSize] = 0;
		}

		pagedFile.close();

		pagedFile.open();

		for (int i = 0; i < FILESIZE; i++) {
			p.setId(i);
			pagedFile.readPage(p);
			assertEquals((i % 255), p.getData()[i % pageSize]);
		}
		pagePool.release(p);
	}

	public void testWritePage() throws IOException {
		final int FILESIZE = 100;
		assertEquals(FILESIZE - 1, pagedFile.addPages(FILESIZE));

		final Page p = pagePool.acquire();
		for (int i = 0; i < FILESIZE; i++) {
			p.setId(i);
			pagedFile.writePage(p);
		}
		pagePool.release(p);
	}

	@Override
	protected void setUp() {
		super.setUp();
		getInstrumentation().getContext().getDatabasePath("test").mkdirs();
		if (pagedFile.exists())
			pagedFile.remove();
		pagedFile.create();
		pagedFile.open();
	}

	@Override
	protected void tearDown() {
		pagedFile.close();
		pagedFile.remove();
	}

}