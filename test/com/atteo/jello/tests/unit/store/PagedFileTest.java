package com.atteo.jello.tests.unit.store;

import java.io.File;
import java.io.IOException;

import android.test.InstrumentationTestCase;

import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagePool;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.store.StoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class PagedFileTest extends InstrumentationTestCase {
	private final String filename = "testfile";
	private Injector injector;
	private int pageSize;
	
	public void testAddPage() throws IOException {
		final int TESTSIZE = 10;
		int id;

		for (int i = 0; i < TESTSIZE; i++) {
			id = PagedFile.addPage();
			assertEquals(i, id);
			assertEquals(i + 1, PagedFile.getPageCount());
			assertEquals(pageSize * (i + 1), PagedFile.length());
		}

	}

	public void testGetPage() throws IOException {
		final PagePool pagePool = injector.getInstance(PagePool.class);
		final Page p = pagePool.acquire();
		final byte[] saved = p.getData();
		PagedFile.addPage();
		PagedFile.writePage(0, p);
		PagedFile.getPage(0, p);
		p.equals(saved);
		pagePool.release(p);
	}

	public void testPagedFile() {
		assertTrue(!PagedFile.isReadOnly());
		assertEquals(0, PagedFile.getPageCount());
		assertEquals(0, PagedFile.getFile().length());
	}

	public void testRemovePage() throws IOException {
		PagedFile.removePage();
		assertEquals(0, PagedFile.length());
		assertEquals(0, PagedFile.getPageCount());

		assertEquals(0, PagedFile.addPage());
		assertEquals(pageSize, PagedFile.length());
		assertEquals(1, PagedFile.getPageCount());

		PagedFile.removePage();
		assertEquals(0, PagedFile.length());
		assertEquals(0, PagedFile.getPageCount());

		PagedFile.removePage();
		assertEquals(0, PagedFile.length());
		assertEquals(0, PagedFile.getPageCount());
	}

	public void testWriteGetPage() throws IOException {
		final int FILESIZE = 100;
		for (int i = 0; i < FILESIZE; i++)
			assertEquals(i, PagedFile.addPage());
		final PagePool pagePool = injector.getInstance(PagePool.class);
		final Page p = pagePool.acquire();
		for (int i = 0; i < FILESIZE; i++) {
			p.getData()[i % pageSize] = (byte) (i % 255);
			PagedFile.writePage(i, p);
			p.getData()[i % pageSize] = 0;
		}
		for (int i = 0; i < FILESIZE; i++) {
			PagedFile.getPage(i, p);
			assertEquals((i % 255), p.getData()[i % pageSize]);
		}
		pagePool.release(p);
	}

	public void testWritePage() throws IOException {
		final int FILESIZE = 100;
		for (int i = 0; i < FILESIZE; i++)
			assertEquals(i, PagedFile.addPage());
		final PagePool pagePool = injector.getInstance(PagePool.class);
		final Page p = pagePool.acquire();
		for (int i = 0; i < FILESIZE; i++)
			PagedFile.writePage(i, p);
		pagePool.release(p);
	}

	@Override
	protected void setUp() throws IOException {
		injector = Guice.createInjector(new StoreModule(null));
		final File f = getInstrumentation().getContext().getDatabasePath(
				filename);
		f.getParentFile().mkdirs();
		f.delete();
		f.createNewFile();
		PagedFile.open(f, false);
		pageSize = PagedFile.getPageSize();

	}

	@Override
	protected void tearDown() {
		PagedFile.getFile().delete();
		PagedFile.close();

	}

}