package com.atteo.jello.store;

import java.io.File;

public interface OSFileFactory {
	OSFile create(File file);
}
