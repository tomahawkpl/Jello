package com.atteo.jello.store;

import java.io.File;

public interface PagedFileFactory {
	PagedFile create(File file, boolean readOnly);
}
