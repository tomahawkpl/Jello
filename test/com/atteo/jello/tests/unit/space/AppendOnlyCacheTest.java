package com.atteo.jello.tests.unit.space;

import com.atteo.jello.space.AppendOnlyCache;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.name.Names;

public abstract class AppendOnlyCacheTest extends
		JelloInterfaceTestCase<AppendOnlyCache> {
	private final int appendOnlyCacheSize = 3;

	@Inject
	private AppendOnlyCache appendOnlyCache;

	public void configure(final Binder binder) {
		binder.bind(Integer.class).annotatedWith(
				Names.named("appendOnlyCacheSize")).toInstance(
				appendOnlyCacheSize);
	}

	public void testOverflow() {
		appendOnlyCache.update(0, (short) 5);
		appendOnlyCache.update(1, (short) 10);
		appendOnlyCache.update(2, (short) 15);
		appendOnlyCache.update(3, (short) 20);
		assertEquals(1, appendOnlyCache.getBestId((short) 1));
		assertEquals(2, appendOnlyCache.getBestId((short) 15));
		assertEquals(3, appendOnlyCache.getBestId((short) 16));
		assertEquals(20, appendOnlyCache.getFreeSpace(3));
		appendOnlyCache.update(2, (short) 0);
		appendOnlyCache.update(3, (short) 0);
		assertEquals(-1, appendOnlyCache.getBestId((short) 11));
		assertEquals(1, appendOnlyCache.getBestId((short) 10));

		assertEquals(10, appendOnlyCache.getFreeSpace(1));
		assertTrue(!appendOnlyCache.isEmpty());
		appendOnlyCache.update(1, (short) 0);
		assertTrue(appendOnlyCache.isEmpty());
	}

	public void testSimple() {
		// is initially empty
		assertTrue(appendOnlyCache.isEmpty());
		// adding an empty page doesn't change anything
		appendOnlyCache.update(0, (short) 0);
		assertTrue(appendOnlyCache.isEmpty());

		// simple page added
		appendOnlyCache.update(0, (short) 10);
		assertEquals(-1, appendOnlyCache.getBestId((short) 11));
		assertEquals(0, appendOnlyCache.getBestId((short) 10));
		assertEquals(0, appendOnlyCache.getBestId((short) 1));
		assertEquals(10, appendOnlyCache.getFreeSpace(0));

		// adding empty page again
		appendOnlyCache.update(1, (short) 0);
		assertEquals(-1, appendOnlyCache.getBestId((short) 11));
		assertEquals(0, appendOnlyCache.getBestId((short) 10));
		assertEquals(0, appendOnlyCache.getBestId((short) 1));
		assertEquals(10, appendOnlyCache.getFreeSpace(0));

		// couple more pages
		appendOnlyCache.update(0, (short) 5);
		assertEquals(-1, appendOnlyCache.getBestId((short) 11));
		assertEquals(-1, appendOnlyCache.getBestId((short) 10));
		assertEquals(0, appendOnlyCache.getBestId((short) 1));
		assertEquals(5, appendOnlyCache.getFreeSpace(0));
		appendOnlyCache.update(1, (short) 15);
		assertEquals(-1, appendOnlyCache.getBestId((short) 16));
		assertEquals(1, appendOnlyCache.getBestId((short) 15));
		assertEquals(0, appendOnlyCache.getBestId((short) 1));
		assertEquals(15, appendOnlyCache.getFreeSpace(1));
		appendOnlyCache.update(0, (short) 0);
		appendOnlyCache.update(1, (short) 0);
		assertTrue(appendOnlyCache.isEmpty());
	}

	@Override
	protected Class<AppendOnlyCache> interfaceUnderTest() {
		return AppendOnlyCache.class;
	}

	@Override
	protected void setUp() {
		super.setUp();
	}

	@Override
	protected void tearDown() {
	}

}
