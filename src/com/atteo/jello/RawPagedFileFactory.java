package com.atteo.jello;

import java.io.File;

public interface RawPagedFileFactory {
	public RawPagedFile create(File file, boolean readOnly);
}
