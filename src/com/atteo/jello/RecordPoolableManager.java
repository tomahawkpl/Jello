package com.atteo.jello;

import android.util.PoolableManager;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class RecordPoolableManager implements PoolableManager<Record> {
	private final Injector injector;

	@Inject
	public RecordPoolableManager(final Injector injector) {
		this.injector = injector;
	}

	public Record newInstance() {
		return injector.getInstance(Record.class);
	}

	public void onAcquired(final Record element) {

	}

	public void onReleased(final Record element) {

	}

}
