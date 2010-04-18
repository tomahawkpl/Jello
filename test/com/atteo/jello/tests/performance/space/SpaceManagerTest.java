package com.atteo.jello.tests.performance.space;

import java.util.HashMap;

import com.atteo.jello.Record;
import com.atteo.jello.space.SpaceManager;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.store.PagedFileNative;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.name.Names;

public abstract class SpaceManagerTest extends
		JelloInterfaceTestCase<SpaceManager> {

	// ---- SETTINGS
	private final short pageSize = 4096;
	private final short blockSize = 128;
	private final short freeSpaceInfosPerPage = 1023;
	private final short freeSpaceInfoPageCapacity = 4092;
	private final short freeSpaceInfoSize = 4;
	private final int freeSpaceMapPageId = 1;
	private final int maxRecordPages = 4;

	// --------------
	
	private final int PAGES = 1000;
	
	@Inject private PagedFile pagedFile;
	@Inject private SpaceManager spaceManager;
	
	@Inject Record record1;
	@Inject Record record2;
	
	@Override
	protected Class<SpaceManager> interfaceUnderTest() {
		return SpaceManager.class;
	}

	public void configure(Binder binder) {
		binder.bind(PagedFile.class).to(PagedFileNative.class);

		String path = getInstrumentation().getContext().getDatabasePath(
				"testfile").getAbsolutePath();
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("pageSize", String.valueOf(pageSize));
		p.put("blockSize", String.valueOf(blockSize));
		p.put("fullpath", String.valueOf(path));
		p.put("maxRecordPages", String.valueOf(maxRecordPages));
		p.put("freeSpaceInfoSize", String.valueOf(freeSpaceInfoSize));
		p.put("freeSpaceInfoPageCapacity", String
				.valueOf(freeSpaceInfoPageCapacity));
		p.put("freeSpaceInfosPerPage", String.valueOf(freeSpaceInfosPerPage));
		p.put("freeSpaceMapPageId", String.valueOf(freeSpaceMapPageId));

		Names.bindProperties(binder, p);
	}

	public void setUp() {
		super.setUp();
		
		if (pagedFile.exists())
			pagedFile.remove();
		
		pagedFile.create();
		pagedFile.open();
		
		pagedFile.addPages(PAGES);
		
		spaceManager.create();
	}
	
	public void tearDown() {
		pagedFile.close();
		pagedFile.remove();
	}
	
	public void testSetPageUsed() {
		int TESTSIZE = 250;
		startPerformanceTest(true);
		
		for (int i=0;i<TESTSIZE;i++) {
			spaceManager.setPageUsed(i % PAGES, (i % 2) == 1);
			spaceManager.setPageUsed((i + 1) % PAGES, (i % 2) == 1);
			spaceManager.setPageUsed((i + 2) % PAGES, (i % 2) == 1);
			spaceManager.setPageUsed((i + 3) % PAGES, (i % 2) == 1);
			spaceManager.commit();
		}
		
		endPerformanceTest();
	}
	
	public void testIsPageUsed() {
		int TESTSIZE = 1000;
		startPerformanceTest(true);
		
		for (int i=0;i<TESTSIZE;i++)
			spaceManager.isPageUsed(i % PAGES);
		
		endPerformanceTest();
	}

	public void testFreeSpaceOnPage() {
		int TESTSIZE = 1000;
		startPerformanceTest(true);
		
		for (int i=0;i<TESTSIZE;i++)
			spaceManager.freeSpaceOnPage(i % PAGES);
		
		endPerformanceTest();
	}
	
	public void testTotalFreeSpace() {
		int TESTSIZE = 100;
		startPerformanceTest(true);
		
		for (int i=0;i<TESTSIZE;i++)
			spaceManager.totalFreeSpace();
		
		endPerformanceTest();
	}
	
	public void testSetBlockUsed() {
		int TESTSIZE = 1000;
		int blocksPerPage = pageSize / blockSize;
		
		startPerformanceTest(true);
		
		for (int i=0;i<TESTSIZE;i++)
			spaceManager.setBlockUsed(i % PAGES, (short) ((i * 2) % blocksPerPage), (i%2) == 1);
		
		endPerformanceTest();
	}
	
	public void testIsBlockUsed() {
		int TESTSIZE = 1000;
		int blocksPerPage = pageSize / blockSize;
		
		startPerformanceTest(true);
		
		for (int i=0;i<TESTSIZE;i++)
			spaceManager.isBlockUsed(i % PAGES, (short) ((i * 2) % blocksPerPage));
		
		endPerformanceTest();
	}
	
	public void testSetRecordUsed() {
		int TESTSIZE = 500;
		
		record1.setChunkUsed(100, (short)0, (short)25, true);
		record1.setChunkUsed(200, (short)20, (short)25, true);
		record1.setChunkUsed(300, (short)60, (short)32, true);
		record1.setChunkUsed(400, (short)100, (short)32, true);
		
		record2.setChunkUsed(150, (short)0, (short)25, true);
		record2.setChunkUsed(250, (short)20, (short)25, true);
		record2.setChunkUsed(350, (short)60, (short)32, true);
		record2.setChunkUsed(450, (short)80, (short)32, true);
		
		startPerformanceTest(true);
		
		for (int i=0;i<TESTSIZE;i++) {
			spaceManager.setRecordUsed(record1, (i%2) == 1);
			spaceManager.setRecordUsed(record2, (i%2) == 1);
		}
		
		endPerformanceTest();
	}
	
	public void testUpdate() {
		int TESTSIZE = 10;
		
		startPerformanceTest(true);
		
		for (int i=0;i<TESTSIZE;i++) {
			pagedFile.addPages(25);
			spaceManager.update();
		}
		
		for (int i=0;i<TESTSIZE;i++) {
			pagedFile.removePages(25);
			spaceManager.update();
		}
		
		endPerformanceTest();
	}
}
