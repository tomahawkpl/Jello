package com.atteo.jello;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class PageUsage {
		@Inject protected static @Named("freeSpaceInfoSize") short freeSpaceInfoSize;
		
		PageUsage() {
			usage = new byte[freeSpaceInfoSize];
			pageId = -1;
			blocksUsed = 0;
		}

		public int pageId;
		public int blocksUsed;
		public byte[] usage;
	}