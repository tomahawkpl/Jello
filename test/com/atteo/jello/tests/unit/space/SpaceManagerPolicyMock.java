package com.atteo.jello.tests.unit.space;

import com.atteo.jello.Record;
import com.atteo.jello.space.SpaceManagerPolicy;

public class SpaceManagerPolicyMock implements SpaceManagerPolicy {

	public int acquirePage() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean acquireRecord(Record record, int length) {
		// TODO Auto-generated method stub
		return false;
	}

	public void create() {
		// TODO Auto-generated method stub

	}

	public boolean load() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean reacquireRecord(Record record, int length) {
		// TODO Auto-generated method stub
		return false;
	}

	public void releasePage(int id) {
		// TODO Auto-generated method stub

	}

	public void releaseRecord(Record record) {
		// TODO Auto-generated method stub

	}

	public void commit() {
		// TODO Auto-generated method stub
		
	}

}
