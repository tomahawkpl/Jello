package com.atteo.jello.store;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class PagePool {

	private final Injector injector;
	private final int limit;
	private int poolCount;
	private Page root;

	@Inject
	public PagePool(final Injector injector,
			@Named("pagePoolLimit") final int limit) {
		this.limit = limit;
		this.injector = injector;
	}

	public Page acquire() {
		Page element;

		if (root != null) {
			element = root;
			root = element.nextInPool;
			poolCount--;
		} else
			element = injector.getInstance(Page.class);

		if (element != null)
			element.nextInPool = null;

		return element;
	}

	public void release(final Page element) {
		if (poolCount < limit) {
			poolCount++;
			element.nextInPool = root;
			root = element;
		}
	}

}
