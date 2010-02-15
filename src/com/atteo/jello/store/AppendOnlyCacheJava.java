package com.atteo.jello.store;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class AppendOnlyCacheJava implements AppendOnlyCache {
	private PriorityQueue<CacheElement> cache;
	private final int limit;

	@Inject
	public AppendOnlyCacheJava(
			@Named("appendOnlyCacheLimit") int appendOnlyCacheLimit) {
		this.limit = appendOnlyCacheLimit;

		cache = new PriorityQueue<CacheElement>(appendOnlyCacheLimit,
				new Comparator<CacheElement>() {

					public int compare(CacheElement object1,
							CacheElement object2) {
						if (object1.freeSpace > object2.freeSpace)
							return -1;
						else {
							if (object1.freeSpace < object2.freeSpace)
								return 1;
							else
								return 0;
						}
					}

				});

	}

	public boolean isEmpty() {
		return cache.isEmpty();
	}

	public long getBestId() {
		return cache.peek().id;
	}

	public int getBestFreeSpace() {
		return cache.peek().freeSpace;
	}

	public void update(long id, int freeSpace) {
		Iterator<CacheElement> iterator = cache.iterator();

		CacheElement e = null;
		boolean removed = false;
		int leastSpace = -1;
		CacheElement leastSpaceElem = null;
		int size = cache.size();
		while (iterator.hasNext()) {
			e = iterator.next();

			if (e.id == id) {
				removed = true;
				iterator.remove();
				break;
			}

			if (size == limit && (leastSpace == -1 || e.freeSpace < leastSpace)) {
				leastSpace = e.freeSpace;
				leastSpaceElem = e;
			}
		}

		if (freeSpace == 0)
			return;

		if (!removed && leastSpace < freeSpace && leastSpace != -1) {
			e = leastSpaceElem;
			cache.remove(e);
			removed = true;
		}

		if (!removed)
			e = new CacheElement();

		e.id = id;
		e.freeSpace = freeSpace;
		cache.offer(e);

	}

	private class CacheElement {
		long id;
		int freeSpace;
	}
}
