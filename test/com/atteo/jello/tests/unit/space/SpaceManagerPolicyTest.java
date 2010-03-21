package com.atteo.jello.tests.unit.space;

import java.io.IOException;
import java.util.ArrayList;

import android.test.InstrumentationTestCase;

import com.atteo.jello.space.AppendOnlyCache;
import com.atteo.jello.space.AppendOnlyCacheNative;
import com.atteo.jello.space.Hybrid;
import com.atteo.jello.space.NextFitHistogram;
import com.atteo.jello.space.SpaceManager;
import com.atteo.jello.space.SpaceManagerNative;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.space.VanillaHistogram;
import com.atteo.jello.store.ListPage;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.tests.unit.store.PagedFileMock;

public class SpaceManagerPolicyTest extends InstrumentationTestCase {
	private SpaceManager spaceManager;
	private SpaceManagerPolicy policy;
	private PagedFile pagedFile;

	@Override
	protected void setUp() throws IOException {
		pagedFile = new PagedFileMock((short) 4096);
		pagedFile.addPages(5);

		spaceManager = new SpaceManagerNative(pagedFile, new ListPage(
				(short) 4096), (short) 4, (short) 1022, (short) 4092, (short) 128);
		spaceManager.create();

		AppendOnlyCache cache = new AppendOnlyCacheNative(5);
		NextFitHistogram histogram = new VanillaHistogram((short) 4096, 8);

		policy = new Hybrid(pagedFile, spaceManager, cache, histogram, 90,
				(short) 4096, (short) 128);
	}

	public void testSimpleAcquirePage() {
		ArrayList<Integer> acquired = new ArrayList<Integer>();
		for (int i = 0; i < 100; i++) {
			int id = policy.acquirePage();
			assertTrue(spaceManager.isPageUsed(id));
			acquired.add(id);
		}

		for (int i = 0; i < acquired.size(); i++) {
			int id = acquired.get(i);
			assertTrue(spaceManager.isPageUsed(id));
			policy.releasePage(id);
			assertFalse(spaceManager.isPageUsed(id));

		}

	}

	@Override
	protected void tearDown() {
	}
}
