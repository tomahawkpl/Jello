package com.atteo.jello.index;

import com.atteo.jello.space.Record;

public interface Index {
	public void addRecord(Record record);
	public void removeRecord(Record record);
	public Record findRecord(Record record);
	
}
