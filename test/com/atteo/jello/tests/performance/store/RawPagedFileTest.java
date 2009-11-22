package com.atteo.jello.tests.performance.store;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagePool;
import com.atteo.jello.store.RawPagedFile;
import com.atteo.jello.store.RawPagedFileFactory;
import com.atteo.jello.store.StoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import android.os.Debug;
import android.test.InstrumentationTestCase;
import android.test.PerformanceTestCase;

public class RawPagedFileTest extends InstrumentationTestCase implements PerformanceTestCase {
	private final String filename = "testfile";
	private final int pageSize = 4096;
	private final int fileSizeLimit = 104857600; // 100mb
	private RawPagedFile rpf;
	private Injector injector;
	
	private Map<String, String> getProperties() {
		Map<String, String> p = new HashMap<String, String>();
		p.put("pageSize", String.valueOf(pageSize));
		p.put("fileSizeLimit", String.valueOf(fileSizeLimit));
		return p;
	}
	
	@Override
	protected	void setUp() throws IOException {
		injector = Guice.createInjector(new StoreModule(getProperties()));
		File f = getInstrumentation().getContext().getDatabasePath(filename);
		f.getParentFile().mkdirs();
		f.delete();
		f.createNewFile();
		RawPagedFileFactory factory = injector.getInstance(RawPagedFileFactory.class);
		rpf = factory.create(f, false);
	}
	
	@Override
	public boolean isPerformanceOnly() {
		return true;
	}


	public void testGetPage() throws IOException {
		int FILESIZE = 100;
		int TESTSIZE = 10000;
		for (int i = 0; i < FILESIZE; i++)
			assertEquals(i, rpf.addPage());
		PagePool pagePool = injector.getInstance(PagePool.class);
		Page p = pagePool.acquire();
		Debug.startMethodTracing("jello/testGetPage");
		for (int i = 0; i < TESTSIZE; i++) {
			rpf.getPage(i % FILESIZE, p);
			pagePool.release(p);
		}
		Debug.stopMethodTracing();
		pagePool.release(p);
	}

	@Override
	public int startPerformance(Intermediates intermediates) {
		
		return 1;
	}

}
