package com.atteo.jello.tests.unit.store;

import com.atteo.jello.store.PagedFile;

public class PagedFileMockTest extends PagedFileTest {

	@Override
	protected Class<? extends PagedFile> implementation() {
		return PagedFileMock.class;
	}

}
