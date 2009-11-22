package com.atteo.jello.tests.unit.store;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.test.InstrumentationTestCase;

import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagePool;
import com.atteo.jello.store.RawPagedFile;
import com.atteo.jello.store.RawPagedFileFactory;
import com.atteo.jello.store.StoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class RawPagedFileTest extends InstrumentationTestCase {
	private final String filename = "testfile";
	private final int fileSizeLimit = 1024 * 1024 * 10; // 10mb
	private Injector injector;
	private final int pageSize = 4096;
	private RawPagedFile rpf;

	public void testAddPage() throws IOException {
		final int TESTSIZE = 10;
		int id;
		final HashMap<String, String> custom = new HashMap<String, String>();
		custom.put("pageSize", String.valueOf(pageSize));
		custom.put("fileSizeLimit", String.valueOf((pageSize * TESTSIZE)));
		final Injector inj = Guice.createInjector(new StoreModule(custom));
		final RawPagedFileFactory f = inj
				.getInstance(RawPagedFileFactory.class);

		rpf = f.create(rpf.getFile(), false);
		for (int i = 0; i < TESTSIZE; i++) {
			id = rpf.addPage();
			assertEquals(i, id);
			assertEquals(i + 1, rpf.getPageCount());
			assertEquals(pageSize * (i + 1), rpf.getOSFile().length());
		}

		id = rpf.addPage();
		assertEquals(-1, id);
		assertEquals(pageSize * TESTSIZE, rpf.getOSFile().length());
	}

	public void testGetPage() throws IOException {
		final PagePool pagePool = injector.getInstance(PagePool.class);
		final Page p = pagePool.acquire();
		final byte[] saved = p.getData();
		rpf.addPage();
		rpf.writePage(0, p);
		rpf.getPage(0, p);
		p.equals(saved);
		pagePool.release(p);
	}

	public void testRawPagedFile() {
		assertTrue(!rpf.isReadOnly());
		assertEquals(0, rpf.getPageCount());
		assertEquals(0, rpf.getFile().length());
	}

	public void testRemovePage() throws IOException {
		rpf.removePage();
		assertEquals(0, rpf.getOSFile().length());
		assertEquals(0, rpf.getPageCount());

		assertEquals(0, rpf.addPage());
		assertEquals(pageSize, rpf.getOSFile().length());
		assertEquals(1, rpf.getPageCount());

		rpf.removePage();
		assertEquals(0, rpf.getOSFile().length());
		assertEquals(0, rpf.getPageCount());

		rpf.removePage();
		assertEquals(0, rpf.getOSFile().length());
		assertEquals(0, rpf.getPageCount());
	}

	public void testWriteGetPage() throws IOException {
		final int FILESIZE = 100;
		for (int i = 0; i < FILESIZE; i++)
			assertEquals(i, rpf.addPage());
		final PagePool pagePool = injector.getInstance(PagePool.class);
		final Page p = pagePool.acquire();
		for (int i = 0; i < FILESIZE; i++) {
			p.getData()[i % pageSize] = (byte) (i % 255);
			rpf.writePage(i, p);
			p.getData()[i % pageSize] = 0;
		}
		for (int i = 0; i < FILESIZE; i++) {
			rpf.getPage(i, p);
			assertEquals((i % 255), p.getData()[i % pageSize]);
		}
		pagePool.release(p);
	}

	public void testWritePage() throws IOException {
		final int FILESIZE = 100;
		for (int i = 0; i < FILESIZE; i++)
			assertEquals(i, rpf.addPage());
		final PagePool pagePool = injector.getInstance(PagePool.class);
		final Page p = pagePool.acquire();
		for (int i = 0; i < FILESIZE; i++)
			rpf.writePage(i, p);
		pagePool.release(p);
	}

	private Map<String, String> getProperties() {
		final Map<String, String> p = new HashMap<String, String>();
		p.put("pageSize", String.valueOf(pageSize));
		p.put("fileSizeLimit", String.valueOf(fileSizeLimit));
		return p;
	}

	@Override
	protected void setUp() throws IOException {
		injector = Guice.createInjector(new StoreModule(getProperties()));
		final File f = getInstrumentation().getContext().getDatabasePath(
				filename);
		f.getParentFile().mkdirs();
		f.delete();
		f.createNewFile();
		final RawPagedFileFactory factory = injector
				.getInstance(RawPagedFileFactory.class);
		rpf = factory.create(f, false);
	}

	@Override
	protected void tearDown() {
		try {
			rpf.getOSFile().close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		rpf.getFile().delete();
	}

}