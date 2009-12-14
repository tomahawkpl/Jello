package com.atteo.jello.store;


public class Record {
	private long pageId = -1;
	private int start;
	private RecordPart parts[];
	private byte content[];


	Record() {
		
	}

	Record(long pageId, int start) {
		this.pageId = pageId;
		this.start = start;
	}
	
	public void load() {
		
	}
	
	public void save() {
		
	}
}
