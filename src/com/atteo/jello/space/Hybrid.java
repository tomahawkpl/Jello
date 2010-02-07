package com.atteo.jello.space;

import com.atteo.jello.store.PagedFile;
import com.google.inject.Injector;

public class Hybrid extends AppendOnly implements SpaceManagerPolicy {

	Hybrid(Injector inject, PagedFile pagedFile, int pageSize, int blockSize,
			int appendOnlyCacheSize) {
		super(inject, pagedFile, pageSize, blockSize, appendOnlyCacheSize);
		
	}

}
