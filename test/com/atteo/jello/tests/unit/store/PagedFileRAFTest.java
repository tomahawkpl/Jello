package com.atteo.jello.tests.unit.store;

import com.atteo.jello.store.PagedFile;
import com.atteo.jello.store.PagedFileRAF;

public class PagedFileRAFTest extends PagedFileTest {

	@Override
	protected Class<? extends PagedFile> implementation() {
		return PagedFileRAF.class;
	}


}
