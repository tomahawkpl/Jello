package com.atteo.jello.space;

import android.util.Poolable;

public class Record implements Poolable<Record> {
	private Record nextPoolable;
	private int id;
	private int schemaVersion;
	private byte content[];

	Record() {

	}

	public Record getNextPoolable() {
		return nextPoolable;
	}

	public void setNextPoolable(Record element) {
		this.nextPoolable = element;
	}

}
