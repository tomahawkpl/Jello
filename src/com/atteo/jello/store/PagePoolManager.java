package com.atteo.jello.store;

import com.atteo.jello.pool.PoolableManager;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;


@Singleton
public class PagePoolManager implements PoolableManager<Page>{
	private Injector injector;
	
	@Inject
	PagePoolManager(Injector injector) {
		this.injector = injector;
	}
	
	@Override
	public Page newInstance() {
		return injector.getInstance(Page.class);
	}

	@Override
	public void onAcquired(Page element) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReleased(Page element) {
		// TODO Auto-generated method stub
		
	}

}
