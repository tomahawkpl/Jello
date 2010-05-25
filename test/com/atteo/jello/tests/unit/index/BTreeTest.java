package com.atteo.jello.tests.unit.index;

import com.atteo.jello.index.BTree;
import com.atteo.jello.index.Index;

public class BTreeTest extends IndexTest {

	@Override
	protected Class<? extends Index> implementation() {
		return BTree.class;
	}

}
