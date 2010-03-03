package com.atteo.jello.store;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Hybrid implements SpaceManagerPolicy {
	private SpaceManager spaceManager;
	private AppendOnlyCache appendOnlyCache;
	private NextFitHistogram nextFitHistogram;
	
	@Inject
	public Hybrid(SpaceManager spaceManager, AppendOnlyCache appendOnlyCache, NextFitHistogram nextFitHistogram) {
		this.spaceManager = spaceManager;
		this.appendOnlyCache = appendOnlyCache;
		this.nextFitHistogram = nextFitHistogram;
	}
	
	public long acquirePage() {
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

	public void releasePage(long id) {
		// TODO Auto-generated method stub

	}

	public void releaseRecordSpace(RecordPart[] parts) {
		// TODO Auto-generated method stub

	}

}
