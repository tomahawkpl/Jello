package com.atteo.jello.store;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;

@Singleton
public class SpaceManager {
	private PagedFile pagedFile;
	private ListPage freeSpaceMap;
	
	@Inject
	SpaceManager(PagedFile pagedFile) {
		this.pagedFile = pagedFile;
	}
	
	RecordPart[] acquireRecordSpace(int length) {
		return null;
	}
	
	RecordPart[] reacquireRecordSpace(int pageId, int start, int length) {
		return null;
	}
	
	void freeRecordSpace(RecordPart parts[]) {
		
	}
	
	
}
