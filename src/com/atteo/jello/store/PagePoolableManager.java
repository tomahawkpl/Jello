package com.atteo.jello.store;

import android.util.PoolableManager;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class PagePoolableManager implements PoolableManager<Page> {

	private final Injector injector;

	@Inject
	public PagePoolableManager(final Injector injector) {
		this.injector = injector;
	}

	public Page newInstance() {
		return injector.getInstance(Page.class);
	}

	public void onAcquired(final Page element) {

	}

	public void onReleased(final Page element) {

	}

}
