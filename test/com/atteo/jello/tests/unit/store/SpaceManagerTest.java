package com.atteo.jello.tests.unit.store;

import java.io.IOException;

import android.test.InstrumentationTestCase;

import com.atteo.jello.store.PagedFile;
import com.atteo.jello.store.SpaceManager;

public class SpaceManagerTest extends InstrumentationTestCase {
	private SpaceManager spaceManager;
	private PagedFile pagedFile;
	
	@Override
	protected void setUp() throws IOException {
		pagedFile = new PagedFileMock(4096);
	}
	
	public void testCreate() {
		
		//spaceManager = new SpaceManager(pagedFile, 128, 4, 4096, appendOnlyCache);
		//spaceManager.create(DatabaseFile.MIN_PAGES);
	}
	
	@Override
	protected void tearDown() {
	}
}
