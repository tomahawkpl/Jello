package com.atteo.jello.store;

import com.google.inject.Provider;

public class PageSizeProvider implements Provider<Short> {
	private short pageSize = -1;
	static {
		System.loadLibrary("PageSizeProvider");
	}

	public Short get() {
		if (pageSize == -1)
			pageSize = getPageSize();
		return pageSize;
	}

	native private short getPageSize();

}
