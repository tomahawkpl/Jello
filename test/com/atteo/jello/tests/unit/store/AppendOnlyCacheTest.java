package com.atteo.jello.tests.unit.store;

import java.io.IOException;

import android.test.InstrumentationTestCase;

import com.atteo.jello.store.AppendOnlyCache;
import com.atteo.jello.store.AppendOnlyCacheNative;

public class AppendOnlyCacheTest extends InstrumentationTestCase {
	private AppendOnlyCache appendOnlyCache;

	
	@Override
	protected void setUp() throws IOException {
		appendOnlyCache = new AppendOnlyCacheNative(3);
	}
	
	public void testSimple() {
		// is initially empty
		assertTrue(appendOnlyCache.isEmpty());
		// adding an empty page doesn't change anything
		appendOnlyCache.update(0, 0);
		assertTrue(appendOnlyCache.isEmpty());
		
		// simple page added
		appendOnlyCache.update(0, 10);
		assertEquals(0, appendOnlyCache.getBestId());
		assertEquals(10, appendOnlyCache.getBestFreeSpace());
		
		// adding empty page again
		appendOnlyCache.update(1, 0);
		assertEquals(0, appendOnlyCache.getBestId());
		assertEquals(10, appendOnlyCache.getBestFreeSpace());
		
		
		// couple more pages
		appendOnlyCache.update(0, 5);
		assertEquals(0, appendOnlyCache.getBestId());
		assertEquals(5, appendOnlyCache.getBestFreeSpace());
		appendOnlyCache.update(1, 15);
		assertEquals(1, appendOnlyCache.getBestId());
		assertEquals(15, appendOnlyCache.getBestFreeSpace());
		appendOnlyCache.update(0, 0);
		appendOnlyCache.update(1, 0);
		assertTrue(appendOnlyCache.isEmpty());
	}
	
	public void testOverflow() {
		appendOnlyCache.update(0, 5);
		appendOnlyCache.update(1, 10);
		appendOnlyCache.update(2, 15);
		appendOnlyCache.update(3, 20);
		assertEquals(3, appendOnlyCache.getBestId());
		assertEquals(20, appendOnlyCache.getBestFreeSpace());
		appendOnlyCache.update(2, 0);
		appendOnlyCache.update(3, 0);
		assertEquals(1, appendOnlyCache.getBestId());
		assertEquals(10, appendOnlyCache.getBestFreeSpace());
		assertTrue(!appendOnlyCache.isEmpty());
		appendOnlyCache.update(1, 0);
		assertTrue(appendOnlyCache.isEmpty());
	}
	
	@Override
	protected void tearDown() {
	}
}
