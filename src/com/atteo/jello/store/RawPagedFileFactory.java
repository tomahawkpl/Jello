package com.atteo.jello.store;

import java.io.File;

public interface RawPagedFileFactory {
	public RawPagedFile create(File file, boolean readOnly);
}
