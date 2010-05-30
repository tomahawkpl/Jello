package com.atteo.jello.tests.unit.klass;

import com.atteo.jello.klass.KlassManager;
import com.atteo.jello.klass.SimpleKlassManager;

public class SimpleKlassManagerTest extends KlassManagerTest {

	@Override
	protected Class<? extends KlassManager> implementation() {
		return SimpleKlassManager.class;
	}

}
