package com.atteo.jello.tests.unit.schema;

import com.atteo.jello.Storable;
import com.atteo.jello.schema.Schema;
import com.atteo.jello.schema.StorableWriter;

public class StorableWriterMock implements StorableWriter {

	public void readStorable(byte[] data, Storable storable, Schema schema) {
		// TODO Auto-generated method stub

	}

	public int writeStorable(byte[] data, Storable storable, Schema schema) {
		// TODO Auto-generated method stub
		return 0;
	}

}
