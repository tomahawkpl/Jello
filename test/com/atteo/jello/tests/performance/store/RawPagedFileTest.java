package com.atteo.jello.tests.performance.store;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.os.Debug;
import android.test.InstrumentationTestCase;
import android.test.PerformanceTestCase;

import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagePool;
import com.atteo.jello.store.RawPagedFile;
import com.atteo.jello.store.RawPagedFileFactory;
import com.atteo.jello.store.StoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class RawPagedFileTest extends InstrumentationTestCase implements
		PerformanceTestCase {
	private final String filename = "testfile";
	private final int fileSizeLimit = 104857600; // 100mb
	private Injector injector;
	private final int pageSize = 4096;
	private RawPagedFile rpf;

	public boolean isPerformanceOnly() {
		return true;
	}

	public int startPerformance(final Intermediates intermediates) {

		return 1;
	}

	public void testGetPage() throws IOException {
		final int FILESIZE = 100;
		final int TESTSIZE = 10000;
		for (int i = 0; i < FILESIZE; i++)
			assertEquals(i, rpf.addPage());
		final PagePool pagePool = injector.getInstance(PagePool.class);
		final Page p = pagePool.acquire();
		Debug.startMethodTracing("jello/testGetPage");
		for (int i = 0; i < TESTSIZE; i++) {
			rpf.getPage(i % FILESIZE, p);
			pagePool.release(p);
		}

		Debug.stopMethodTracing();
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

}
