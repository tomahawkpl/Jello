package com.atteo.jello.tests.unit.space;

import com.atteo.jello.Record;
import com.atteo.jello.space.SpaceManagerPolicy;

public class SpaceManagerPolicyMock implements SpaceManagerPolicy {

	public int acquirePage() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean acquireRecord(final Record record, final int length) {
		// TODO Auto-generated method stub
		return false;
	}

	public void commit() {
		// TODO Auto-generated method stub

	}

	public void create() {
		// TODO Auto-generated method stub

	}

	public boolean isPageUsed(final int id) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean load() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean reacquireRecord(final Record record, final int length) {
		// TODO Auto-generated method stub
		return false;
	}

	public void releasePage(final int id) {
		// TODO Auto-generated method stub

	}

	public void releaseRecord(final Record record) {
		// TODO Auto-generated method stub

	}

	public void setPageUsed(final int id, final boolean used) {
		// TODO Auto-generated method stub

	}

}
