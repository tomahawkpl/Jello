package com.atteo.jello.tests.unit;

import com.atteo.jello.KlassManager;
import com.atteo.jello.SimpleKlassManager;

public class SimpleKlassManagerTest extends KlassManagerTest {

	@Override
	protected Class<? extends KlassManager> implementation() {
		return SimpleKlassManager.class;
	}

}
