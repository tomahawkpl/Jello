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
		assertEquals(-1, appendOnlyCache.getBestId(11));
		assertEquals(0, appendOnlyCache.getBestId(10));
		assertEquals(0, appendOnlyCache.getBestId(1));
		assertEquals(10, appendOnlyCache.getFreeSpace(0));
		
		// adding empty page again
		appendOnlyCache.update(1, 0);
		assertEquals(-1, appendOnlyCache.getBestId(11));
		assertEquals(0, appendOnlyCache.getBestId(10));
		assertEquals(0, appendOnlyCache.getBestId(1));
		assertEquals(10, appendOnlyCache.getFreeSpace(0));
		
		
		// couple more pages
		appendOnlyCache.update(0, 5);
		assertEquals(-1, appendOnlyCache.getBestId(11));
		assertEquals(-1, appendOnlyCache.getBestId(10));
		assertEquals(0, appendOnlyCache.getBestId(1));
		assertEquals(5, appendOnlyCache.getFreeSpace(0));
		appendOnlyCache.update(1, 15);
		assertEquals(-1, appendOnlyCache.getBestId(16));
		assertEquals(1, appendOnlyCache.getBestId(15));
		assertEquals(0, appendOnlyCache.getBestId(1));
		assertEquals(15, appendOnlyCache.getFreeSpace(1));
		appendOnlyCache.update(0, 0);
		appendOnlyCache.update(1, 0);
		assertTrue(appendOnlyCache.isEmpty());
	}
	
	public void testOverflow() {
		appendOnlyCache.update(0, 5);
		appendOnlyCache.update(1, 10);
		appendOnlyCache.update(2, 15);
		appendOnlyCache.update(3, 20);
		assertEquals(1, appendOnlyCache.getBestId(1));
		assertEquals(2, appendOnlyCache.getBestId(15));
		assertEquals(3, appendOnlyCache.getBestId(16));
		assertEquals(20, appendOnlyCache.getFreeSpace(3));
		appendOnlyCache.update(2, 0);
		appendOnlyCache.update(3, 0);
		assertEquals(-1, appendOnlyCache.getBestId(11));
		assertEquals(1, appendOnlyCache.getBestId(10));

		assertEquals(10, appendOnlyCache.getFreeSpace(1));
		assertTrue(!appendOnlyCache.isEmpty());
		appendOnlyCache.update(1, 0);
		assertTrue(appendOnlyCache.isEmpty());
	}
	
	@Override
	protected void tearDown() {
	}
}
