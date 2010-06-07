package com.atteo.jello.index;

import com.atteo.jello.Record;

public interface Index {
	public void commit();

	public void create();

	public boolean find(Record record);

	public void insert(Record record);

	public boolean load();

	public void remove(int id);

	public void update(Record record);
	
	public void iterate();
	public int nextId();
}