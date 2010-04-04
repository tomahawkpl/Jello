package com.atteo.jello;

import com.google.inject.Inject;
import com.google.inject.name.Named;

class PageUsage {
		@Inject
		private PageUsage(@Named("freeSpaceInfoSize") short freeSpaceInfoSize) {
			usage = new byte[freeSpaceInfoSize];
			pageId = -1;
			blocksUsed = 0;
		}

		int pageId;
		int blocksUsed;
		byte[] usage;
	}