package com.atteo.jello.space;

import com.atteo.jello.store.PagedFile;
import com.atteo.jello.store.RecordPart;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Hybrid implements SpaceManagerPolicy {
	private SpaceManager spaceManager;
	private AppendOnlyCache appendOnlyCache;
	private NextFitHistogram nextFitHistogram;
	private PagedFile pagedFile;

	@Inject
	public Hybrid(PagedFile pagedFile, SpaceManager spaceManager,
			AppendOnlyCache appendOnlyCache, NextFitHistogram nextFitHistogram) {
		this.spaceManager = spaceManager;
		this.appendOnlyCache = appendOnlyCache;
		this.nextFitHistogram = nextFitHistogram;
		this.pagedFile = pagedFile;
		
		for (int i=0;i<pagedFile.getPageCount();i++) {
			
			
		}
		
	}

	public int acquirePage() {
		// TODO Auto-generated method stub
		return 0;
	}

	public RecordPart[] acquireRecordSpace(int length) {
		// TODO Auto-generated method stub
		return null;
	}

	public RecordPart[] reacquireRecordSpace(RecordPart[] parts, int length) {
		// TODO Auto-generated method stub
		return null;
	}

	public void releasePage(int id) {
		// TODO Auto-generated method stub

	}

	public void releaseRecordSpace(RecordPart[] parts) {
		// TODO Auto-generated method stub

	}

}
