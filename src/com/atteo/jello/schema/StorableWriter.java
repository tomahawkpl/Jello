package com.atteo.jello.schema;

import com.atteo.jello.Storable;

public interface StorableWriter {
	byte[] writeStorable(Storable storable, Schema schema);
	void readStorable(byte[] data, Storable storable, Schema schema);
}