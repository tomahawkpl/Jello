package com.atteo.jello.tests.unit.schema;

import com.atteo.jello.schema.SchemaManager;
import com.atteo.jello.schema.SimpleSchemaManager;

public class SimpleSchemaManagerTest extends SchemaManagerTest {

	@Override
	protected Class<? extends SchemaManager> implementation() {
		return SimpleSchemaManager.class;
	}

}
