package com.atteo.jello.store;

import com.google.inject.Inject;
import com.google.inject.Injector;

import android.util.PoolableManager;

public class PagePoolableManager implements PoolableManager<Page> {

	private Injector injector;
	
	@Inject
	public PagePoolableManager(Injector injector) {
		this.injector = injector;
	}
	
	public Page newInstance() {
		return injector.getInstance(Page.class);
	}

	public void onAcquired(Page element) {
		
	}

	public void onReleased(Page element) {
		
	}

}
