package com.atteo.jello.space;

import com.google.inject.Inject;
import com.google.inject.Injector;

import android.util.PoolableManager;

public class RecordPoolableManager implements PoolableManager<Record> {
	private Injector injector;
	
	@Inject
	public RecordPoolableManager(Injector injector) {
		this.injector = injector;
	}
	
	public Record newInstance() {
		return injector.getInstance(Record.class);
	}

	public void onAcquired(Record element) {
		
	}

	public void onReleased(Record element) {
		
	}

}
