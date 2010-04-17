package com.atteo.jello.tests.unit.schema;

import com.atteo.jello.schema.StorableWriter;
import com.atteo.jello.schema.VanillaStorableWriter;

public class VanillaStorableWriterTest extends StorableWriterTest {

	@Override
	protected Class<? extends StorableWriter> implementation() {
		return VanillaStorableWriter.class;
	}

}
