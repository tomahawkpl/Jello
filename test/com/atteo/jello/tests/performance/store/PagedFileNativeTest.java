package com.atteo.jello.tests.performance.store;

import com.atteo.jello.store.PagedFile;
import com.atteo.jello.store.PagedFileNative;
import com.atteo.jello.tests.performance.store.PagedFileTest;

public class PagedFileNativeTest extends PagedFileTest {

	@Override
	protected Class<? extends PagedFile> implementation() {
		return PagedFileNative.class;
	}

}
