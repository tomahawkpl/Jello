package com.atteo.jello.store;

import com.atteo.jello.pool.FinitePool;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;


@Singleton
public class PagePool extends FinitePool<Page> {

	@Inject
	public PagePool(PagePoolManager manager, @Named("pagePoolLimit") int limit) {
		super(manager, limit);
	}

}
