package com.atteo.jello.tests.performance.store;

import java.io.File;
import java.io.IOException;

import android.os.Debug;
import android.test.InstrumentationTestCase;
import android.test.PerformanceTestCase;
import android.util.Pool;

import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.store.StoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class PagedFileTest extends InstrumentationTestCase implements
		PerformanceTestCase {
	private static final String filename = "testfile";
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
		final Pool<Page> pagePool = injector.getInstance(Key.get(new TypeLiteral<Pool<Page>>(){}));
		Page p = pagePool.acquire();
		int seed = 1112;
		
		Debug.startMethodTracing("jello/testGetPage");
		for (int i = 0; i < TESTSIZE; i++) {
			p.setId(seed % FILESIZE);
			pagedFile.readPage(p);
			seed = ((seed*seed)/10)%10000;
		}
		Debug.stopMethodTracing();
		
	}
	
	public void testWritePage() throws IOException {
		final int FILESIZE = 200;
		final int TESTSIZE = 10000;
		assertEquals(FILESIZE-1, pagedFile.addPages(FILESIZE));
		final Pool<Page> pagePool = injector.getInstance(Key.get(new TypeLiteral<Pool<Page>>(){}));
		Page p = pagePool.acquire();
		int seed = 3432;
		p.getData()[7]='a';
		Debug.startMethodTracing("jello/testWritePage");
		
		for (int i = 0; i < TESTSIZE; i++) {
			p.setId(seed % FILESIZE);
			pagedFile.writePage(p);
			seed = ((seed*seed)/10)%10000;
		}

		Debug.stopMethodTracing();
	}
	

	@Override
	protected void setUp() throws IOException {
		f = getInstrumentation().getContext().getDatabasePath(
				filename);
		injector = Guice.createInjector(new StoreModule(f.getAbsolutePath(),null));

		f.getParentFile().mkdirs();
		if (f.exists())
			f.delete();
		f.createNewFile();

		pagedFile = injector.getInstance(PagedFile.class);
		pagedFile.open();
	}
	
	@Override
	protected void tearDown() {
		pagedFile.close();

		//f.delete();
	}

}
