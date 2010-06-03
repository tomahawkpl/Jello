package com.atteo.jello.space;

import com.atteo.jello.Record;

public interface SpaceManager {
	void commit();

	void create();

	short freeSpaceOnPage(int id);

	boolean isBlockUsed(int id, short block);

	boolean isPageUsed(int id);

	boolean load();

	void setBlockUsed(int id, short block, boolean used);

	void setPageUsed(int id, boolean used);

	void setRecordUsed(Record record, boolean used);

	long totalFreeSpace();

	void update();
}