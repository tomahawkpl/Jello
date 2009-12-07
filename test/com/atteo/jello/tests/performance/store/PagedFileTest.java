package com.atteo.jello.tests.performance.store;

import java.io.File;
import java.io.IOException;

import android.os.Debug;
import android.test.InstrumentationTestCase;
import android.test.PerformanceTestCase;

import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagePool;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.store.PagedFileFactory;
import com.atteo.jello.store.StoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class PagedFileTest extends InstrumentationTestCase implements
		PerformanceTestCase {
	private final String filename = "testfile";
	private Injector injector;
	private PagedFile pagedFile;
	private File f;
	
	public boolean isPerformanceOnly() {
		return true;
	}

	public int startPerformance(final Intermediates intermediates) {

		return 1;
	}

	public void testGetPage() throws IOException {
		final int FILESIZE = 200;
		final int TESTSIZE = 10000;
		assertEquals(FILESIZE-1, pagedFile.addPages(FILESIZE));
		final PagePool pagePool = injector.getInstance(PagePool.class);
		Page p = pagePool.acquire();
		int seed = 1112;
		
		Debug.startMethodTracing("jello/testGetPage");
		for (int i = 0; i < TESTSIZE; i++) {
			pagedFile.readPage(seed % FILESIZE, p.getData());
			seed = ((seed*seed)/10)%10000;
		}
		Debug.stopMethodTracing();
		
	}
	
	public void testWritePage() throws IOException {
		final int FILESIZE = 200;
		final int TESTSIZE = 10000;
		assertEquals(FILESIZE-1, pagedFile.addPages(FILESIZE));
		final PagePool pagePool = injector.getInstance(PagePool.class);
		Page p = pagePool.acquire();
		int seed = 3432;
		
		Debug.startMethodTracing("jello/testWritePage");
		
		for (int i = 0; i < TESTSIZE; i++) {
			pagedFile.writePage(seed % FILESIZE, p.getData());
			seed = ((seed*seed)/10)%10000;
		}

		Debug.stopMethodTracing();
	}
	

	@Override
	protected void setUp() throws IOException {
		injector = Guice.createInjector(new StoreModule(null));
		f = getInstrumentation().getContext().getDatabasePath(
				filename);
		f.getParentFile().mkdirs();
		if (f.exists())
			f.delete();
		f.createNewFile();
		PagedFileFactory pfFactory = injector.getInstance(PagedFileFactory.class);
		pagedFile = pfFactory.create(f, false);
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
