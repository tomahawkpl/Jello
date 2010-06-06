package com.atteo.jello.schema;

import com.atteo.jello.Storable;

public interface StorableWriter {
	public static final int EMPTY_STORABLE = -1;
	void readStorable(byte[] data, Storable storable, Schema schema);

	int writeStorable(byte[] data, Storable storable, Schema schema);
}