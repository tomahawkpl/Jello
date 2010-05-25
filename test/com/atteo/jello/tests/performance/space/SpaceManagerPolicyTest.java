package com.atteo.jello.tests.performance.space;

import java.util.HashMap;

import android.util.Log;
import android.util.Pool;

import com.atteo.jello.PageUsage;
import com.atteo.jello.Record;
import com.atteo.jello.space.SpaceManager;
import com.atteo.jello.space.SpaceManagerNative;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.store.PagedFileNative;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.name.Names;

public abstract class SpaceManagerPolicyTest extends
		JelloInterfaceTestCase<SpaceManagerPolicy> {

	@Inject
	SpaceManagerPolicy policy;
	@Inject
	PagedFile pagedFile;
	@Inject
	SpaceManager spaceManager;
	@Inject
	Pool<Record> recordPool;
	
	// ---- SETTINGS
	private final short pageSize = 4096;
	private final short blockSize = 32;
	private final short freeSpaceInfosPerPage = 127;
	private final short freeSpaceInfoPageCapacity = 4092;
	private final short freeSpaceInfoSize = 16;
	private final int freeSpaceInfoPageId = 1;
	private final int maxRecordPages = 4;
	private final int maxRecordSize = maxRecordPages * pageSize;

	// --------------

	@Override
	protected Class<SpaceManagerPolicy> interfaceUnderTest() {
		return SpaceManagerPolicy.class;
	}

	public void configure(Binder binder) {
		binder.requestStaticInjection(Page.class);
		binder.requestStaticInjection(PageUsage.class);
		binder.requestStaticInjection(Record.class);
		
		binder.bind(PagedFile.class).to(PagedFileNative.class);
		binder.bind(SpaceManager.class).to(SpaceManagerNative.class);

		String path = getInstrumentation().getContext().getDatabasePath(
				"testfile").getAbsolutePath();
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("pageSize", String.valueOf(pageSize));
		p.put("blockSize", String.valueOf(blockSize));
		p.put("fullpath", String.valueOf(path));
		p.put("maxRecordPages", String.valueOf(maxRecordPages));
		p.put("maxRecordSize", String.valueOf(maxRecordSize));
		p.put("freeSpaceInfoSize", String.valueOf(freeSpaceInfoSize));
		p.put("freeSpaceInfoPageCapacity", String
				.valueOf(freeSpaceInfoPageCapacity));
		p.put("freeSpaceInfosPerPage", String.valueOf(freeSpaceInfosPerPage));
		p.put("freeSpaceInfoPageId", String.valueOf(freeSpaceInfoPageId));

		Names.bindProperties(binder, p);
	}

	public void setUp() {
		super.setUp();

		if (pagedFile.exists())
			pagedFile.remove();
		pagedFile.create();
		pagedFile.open();

		pagedFile.addPages(2);

		policy.create();

	}

	public void tearDown() {
		pagedFile.close();
		pagedFile.remove();
	}

	public void testAcquirePage() {
		int TESTSIZE = 100;
		startPerformanceTest(true);

		for (int i = 0; i < TESTSIZE; i++) {
			policy.acquirePage();
		}

		endPerformanceTest();
	}

	public void testReleasePage() {
		int TESTSIZE = 100;

		for (int i = 0; i < TESTSIZE; i++) {
			policy.acquirePage();
		}

		startPerformanceTest(true);

		for (int i = 0; i < TESTSIZE; i++) {
			policy.releasePage(TESTSIZE + 1 - i);
		}

		endPerformanceTest();
	}

	public void testAcquireSmallRecords() {
		int TESTSIZE = 100;
		Record r = recordPool.acquire();
		
		startPerformanceTest(true);

		for (int i = 0; i < TESTSIZE; i++) {
			policy.acquireRecord(r, pageSize / 16);
			r.clearUsage();
		}

		endPerformanceTest();
	}

	public void testAcquireBigRecords() {
		int TESTSIZE = 100;
		Record r = recordPool.acquire();

		startPerformanceTest(true);

		for (int i = 0; i < TESTSIZE; i++) {
			policy.acquireRecord(r, (maxRecordSize / 8) * (i % 8));
			r.clearUsage();
		}

		endPerformanceTest();
	}

	public void testExtensive() {
		int TESTSIZE = 100;

		Record r1, r2;

		r1 = recordPool.acquire();
		r2 = recordPool.acquire();
		
		startPerformanceTest(true);

		policy.acquireRecord(r1, (int) (Math.random() * pageSize));

		for (int i = 0; i < TESTSIZE; i++) {
			r1 = recordPool.acquire();
			r2 = recordPool.acquire();
			policy.acquireRecord(r1, (int) (Math.random() * (pageSize / 2)));
			policy.acquireRecord(r2, (int) (Math.random() * pageSize));
			
			policy.releaseRecord(r1);
			recordPool.release(r1);
			r1 = r2;
		}

		endPerformanceTest();
		
		Log.i("jello","Average freespace: " + (spaceManager.totalFreeSpace() / pagedFile.getPageCount()));

	}

}
