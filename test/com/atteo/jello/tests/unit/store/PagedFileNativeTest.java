package com.atteo.jello.tests.unit.store;

import com.atteo.jello.store.PagedFile;
import com.atteo.jello.store.PagedFileNative;

public class PagedFileNativeTest extends PagedFileTest {

	@Override
	protected Class<? extends PagedFile> implementation() {
		return PagedFileNative.class;
	}

}
