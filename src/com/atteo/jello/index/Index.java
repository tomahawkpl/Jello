package com.atteo.jello.index;

import com.atteo.jello.Record;

public interface Index {
	public void create(int pageId);
	public void load(int pageId);
	public void insert(Record record);
	public void update(Record record);
	public boolean find(Record record);
	public void remove(int id);
}