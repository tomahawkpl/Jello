package com.atteo.jello.tests.performance.store;

import java.io.IOException;
import java.util.HashMap;

import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.name.Names;

public abstract class PagedFileTest extends JelloInterfaceTestCase<PagedFile> {
	@Inject
	private PagedFile pagedFile;
	@Inject
	Page page;

	private final short pageSize = 4096;

	public void configure(final Binder binder) {
		final String path = getInstrumentation().getContext().getDatabasePath(
				"testfile").getAbsolutePath();
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("pageSize", String.valueOf(pageSize));
		p.put("fullpath", String.valueOf(path));
		Names.bindProperties(binder, p);
	}

	public void testGetPage() throws IOException {
		final int FILESIZE = 200;
		final int TESTSIZE = 10000;
		assertEquals(FILESIZE - 1, pagedFile.addPages(FILESIZE));

		int seed = 1112;

		startPerformanceTest(true);

		for (int i = 0; i < TESTSIZE; i++) {
			page.setId(seed % FILESIZE);
			pagedFile.readPage(page);
			seed = seed * seed / 10 % 10000;
		}

		endPerformanceTest();

	}

	public void testWritePage() throws IOException {
		final int FILESIZE = 200;
		final int TESTSIZE = 10000;
		assertEquals(FILESIZE - 1, pagedFile.addPages(FILESIZE));

		int seed = 3432;
		page.getData()[7] = 'a';

		startPerformanceTest(true);

		for (int i = 0; i < TESTSIZE; i++) {
			page.setId(seed % FILESIZE);
			pagedFile.writePage(page);
			seed = seed * seed / 10 % 10000;
		}

		endPerformanceTest();
	}

	@Override
	protected Class<PagedFile> interfaceUnderTest() {
		return PagedFile.class;
	}

	@Override
	protected void setUp() {
		super.setUp();
		if (pagedFile.exists())
			pagedFile.remove();
		pagedFile.create();
		pagedFile.open();
	}

	@Override
	protected void tearDown() {
		pagedFile.close();
	}

}
