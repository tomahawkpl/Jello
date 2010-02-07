package com.atteo.jello.space;

import com.atteo.jello.store.RecordPart;

public class NextFit implements SpaceManagerPolicy {

	public long acquirePage() {
		// TODO Auto-generated method stub
		return 0;
	}

	public RecordPart[] acquireRecordSpace(int length) {
		// TODO Auto-generated method stub
		return null;
	}

	public void create() {
		// TODO Auto-generated method stub

	}

	public boolean load() {
		// TODO Auto-generated method stub
		return false;
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
