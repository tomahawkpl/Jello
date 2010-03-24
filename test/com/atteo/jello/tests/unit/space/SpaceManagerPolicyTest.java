package com.atteo.jello.tests.unit.space;

import java.util.HashMap;

import com.atteo.jello.space.AppendOnlyCache;
import com.atteo.jello.space.AppendOnlyCacheNative;
import com.atteo.jello.space.NextFitHistogram;
import com.atteo.jello.space.SpaceManager;
import com.atteo.jello.space.SpaceManagerNative;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.space.VanillaHistogram;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.atteo.jello.tests.unit.store.PagedFileMock;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.name.Names;


public abstract class SpaceManagerPolicyTest extends JelloInterfaceTestCase<SpaceManagerPolicy> {
	// ---- SETTINGS
	private final short pageSize = 4096;
	private final short blockSize = 128;
	private final short histogramClasses = 128;
	private final short freeSpaceInfosPerPage = 1023;
	private final short freeSpaceInfoPageCapacity = 4092;
	private final short freeSpaceInfoSize = 4;
	private final short appendOnlyCacheSize = 5;


	private final int hybridThreshold = 90;
	// --------------
	
	@Inject private SpaceManagerPolicy policy;
	
	@Override
	protected Class<SpaceManagerPolicy> classUnderTest() {
		return SpaceManagerPolicy.class;
	}
	
	public void configure(Binder binder) {
		binder.bind(PagedFile.class).to(PagedFileMock.class);
		binder.bind(SpaceManager.class).to(SpaceManagerNative.class);
		binder.bind(AppendOnlyCache.class).to(AppendOnlyCacheNative.class);
		binder.bind(NextFitHistogram.class).to(VanillaHistogram.class);
		
		HashMap<String, String> p = new HashMap<String, String>();
		p.put("blockSize", String.valueOf(blockSize));
		p.put("hybridThreshold", String.valueOf(hybridThreshold));
		p.put("pageSize", String.valueOf(pageSize));
		p.put("histogramClasses", String.valueOf(histogramClasses));
		p.put("appendOnlyCacheSize", String.valueOf(appendOnlyCacheSize));
		p.put("freeSpaceInfoSize", String.valueOf(freeSpaceInfoSize));
		p.put("freeSpaceInfoPageCapacity", String.valueOf(freeSpaceInfoPageCapacity));
		p.put("freeSpaceInfosPerPage", String.valueOf(freeSpaceInfosPerPage));

		Names.bindProperties(binder, p);
	}

	@Override
	protected void setUp() {
		
	}

	public void testSimpleAcquirePage() {
		assertEquals(0, policy.acquirePage());
		assertEquals(2, policy.acquirePage());
		
	}

	@Override
	protected void tearDown() {
	}
}
