package com.atteo.jello.schema;

import com.atteo.jello.Storable;

public interface StorableWriter {
	void readStorable(byte[] data, Storable storable, Schema schema);

	int writeStorable(byte[] data, Storable storable, Schema schema);
}