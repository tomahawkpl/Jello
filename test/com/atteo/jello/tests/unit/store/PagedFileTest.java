package com.atteo.jello.tests.unit.store;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import android.test.InstrumentationTestCase;

import com.atteo.jello.store.OSInfo;
import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagePool;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.store.StoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class PagedFileTest extends InstrumentationTestCase {
	private static final String filename = "testfile";
	private Injector injector;
	private PagedFile pagedFile;
	private int pageSize = OSInfo.getPageSize();
	private File f;
	
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

	public void testReadPage() throws IOException {
		final PagePool pagePool = injector.getInstance(PagePool.class);
		final Page p = pagePool.acquire();
		final byte[] saved = p.getData();
		pagedFile.addPages(1);
		p.setId(0);
		pagedFile.writePage(p);
		pagedFile.readPage(p);
		assertTrue(Arrays.equals(p.getData(),saved));
		pagePool.release(p);
	}

	public void testPagedFile() {
		assertTrue(!pagedFile.isReadOnly());
		assertEquals(0, pagedFile.getPageCount());
		assertEquals(0, pagedFile.getFileLength());
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
		try {
			assertEquals(FILESIZE-1, pagedFile.addPages(FILESIZE));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		final PagePool pagePool = injector.getInstance(PagePool.class);
		final Page p = pagePool.acquire();
		
		for (int i = 0; i < FILESIZE; i++) {
			p.setId(i);
			p.getData()[i % pageSize] = (byte) (i % 255);
			pagedFile.writePage(p);
			p.getData()[i % pageSize] = 0;
		}
		
		try {
			pagedFile.close();
			pagedFile.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < FILESIZE; i++) {
			p.setId(i);
			pagedFile.readPage(p);
			assertEquals((i % 255), p.getData()[i % pageSize]);
		}
		pagePool.release(p);
	}

	public void testWritePage() throws IOException {
		final int FILESIZE = 100;
		assertEquals(FILESIZE-1, pagedFile.addPages(FILESIZE));

		final PagePool pagePool = injector.getInstance(PagePool.class);
		final Page p = pagePool.acquire();
		for (int i = 0; i < FILESIZE; i++) {
			p.setId(i);
			pagedFile.writePage(p);
		}
		pagePool.release(p);
	}

	@Override
	protected void setUp() throws IOException {
		HashMap<String, String> properties = new HashMap<String,String>();
		properties.put("pageSize", String.valueOf(pageSize));
		f = getInstrumentation().getContext().getDatabasePath(
				filename);
		f.getParentFile().mkdirs();
		if (f.exists())
			f.delete();
		f.createNewFile();
		injector = Guice.createInjector(new StoreModule(f.getAbsolutePath(),properties));

		pagedFile = injector.getInstance(PagedFile.class);
		pagedFile.open();

	}

	@Override
	protected void tearDown() {
		try {
			pagedFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		f.delete();

	}

}