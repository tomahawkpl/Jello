package com.atteo.jello.store;

import com.google.inject.Inject;

public class SpaceManager {
	private PagedFile pagedFile;
	
	@Inject
	SpaceManager(PagedFile pagedFile) {
		this.pagedFile = pagedFile;
	}
	
	void acquireRecordSpace(int length) {
		
	}
	
	void reacquireRecordSpace(int pageId, int start, int length) {
		
	}
	
	void freeRecordSpace(RecordPart parts[]) {
		
	}
}
