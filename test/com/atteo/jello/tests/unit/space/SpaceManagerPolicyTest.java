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

public abstract class SpaceManagerPolicyTest extends
		JelloInterfaceTestCase<SpaceManagerPolicy> {
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
	
	@Inject
	private SpaceManagerPolicy policy;

	@Inject
	private PagedFile pagedFile;
	@Inject
	private SpaceManager spaceManager;
	@Inject
	private AppendOnlyCache appendOnlyCache;
	@Inject
	private NextFitHistogram nextFitHistogram;

	@Override
	protected Class<SpaceManagerPolicy> classUnderTest() {
		return SpaceManagerPolicy.class;
	}
	
	public void configure(final Binder binder) {
		binder.bind(PagedFile.class).to(PagedFileMock.class);
		binder.bind(SpaceManager.class).to(SpaceManagerNative.class);
		binder.bind(AppendOnlyCache.class).to(AppendOnlyCacheNative.class);
		binder.bind(NextFitHistogram.class).to(VanillaHistogram.class);

		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("blockSize", String.valueOf(blockSize));
		p.put("hybridThreshold", String.valueOf(hybridThreshold));
		p.put("pageSize", String.valueOf(pageSize));
		p.put("histogramClasses", String.valueOf(histogramClasses));
		p.put("appendOnlyCacheSize", String.valueOf(appendOnlyCacheSize));
		p.put("freeSpaceInfoSize", String.valueOf(freeSpaceInfoSize));
		p.put("freeSpaceInfoPageCapacity", String
				.valueOf(freeSpaceInfoPageCapacity));
		p.put("freeSpaceInfosPerPage", String.valueOf(freeSpaceInfosPerPage));

		Names.bindProperties(binder, p);
	}

	public void testSimpleAcquirePage() {
		assertEquals(2, policy.acquirePage());
		assertEquals(3, policy.acquirePage());

		appendOnlyCache.update(0, pageSize);

		assertEquals(4, policy.acquirePage());

		nextFitHistogram.update(0, (short) -1, pageSize);

		policy.releasePage(2);

		assertEquals(2, policy.acquirePage());
	}
	
	public void testAverageFreeSpace() {
		
	}

	@Override
	protected void setUp() {
		pagedFile.addPages(2);
		spaceManager.create();
	}

	@Override
	protected void tearDown() {
	}
}
