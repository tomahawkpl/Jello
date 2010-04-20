package com.atteo.jello;

import android.util.PoolableManager;

import com.google.inject.Singleton;

@Singleton
public class RecordPoolableManager implements PoolableManager<Record> {

	public RecordPoolableManager() {
	}

	public Record newInstance() {
		return new Record();
	}

	public void onAcquired(final Record element) {
	}

	public void onReleased(final Record element) {
		element.clearUsage();
	}

}
