package com.atteo.jello.tests.performance.schema;

import com.atteo.jello.schema.StorableWriter;
import com.atteo.jello.schema.VanillaStorableWriter;

public class VanillaStorableWriterTest extends StorableWriterTest {

	@Override
	protected Class<? extends StorableWriter> implementation() {
		return VanillaStorableWriter.class;
	}

}
