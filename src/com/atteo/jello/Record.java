package com.atteo.jello;

import android.util.Poolable;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

public class Record implements Poolable<Record> {
	private Record nextPoolable;
	private int id;
	private int schemaVersion;
	private PageUsage[] pages;
	private int maxRecordPages;
	private int pagesUsed;

	@Inject
	Record(final Injector injector,
			@Named("maxRecordPages") final int maxRecordPages) {

		this.maxRecordPages = maxRecordPages;

		pages = new PageUsage[maxRecordPages];

		for (int i = 0; i < maxRecordPages; i++)
			pages[i] = injector.getInstance(PageUsage.class);

		pagesUsed = 0;

	}

	public int getId() {
		return id;
	}

	public Record getNextPoolable() {
		return nextPoolable;
	}

	public int getSchemaVersion() {
		return schemaVersion;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void setNextPoolable(final Record element) {
		nextPoolable = element;
	}

	public void setSchemaVersion(final int schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public void setChunkUsed(int pageId, short start, short end, boolean used) {
		int index = -1;
		int empty = -1;
		for (int i = 0; i < maxRecordPages; i++) {
			if (pages[i].pageId == pageId) {
				index = i;
				break;
			}

			if (pages[i].pageId == -1 && empty == -1)
				empty = i;
		}

		if (index == -1) {
			if (empty == -1)
				throw new IllegalArgumentException("Record can use up to "
						+ String.valueOf(maxRecordPages) + " pages");

			if (used == false)
				return;

			index = empty;
			pagesUsed++;
			pages[index].pageId = pageId;

		}

		if (used) {
			pages[index].blocksUsed += end - start;
			for (int i = start; i < end; i++)
				pages[index].usage[i / Byte.SIZE] |= 1 << (i % Byte.SIZE);
		} else {
			pages[index].blocksUsed -= end - start;
			if (pages[index].blocksUsed == 0) {
				pages[index].pageId = -1;
				pagesUsed--;
			}
			for (int i = start; i < end; i++)
				pages[index].usage[i / Byte.SIZE] &= ~(1 << (i % Byte.SIZE));
		}
	}

	public PageUsage getPageUsage(int page) {
		int p = 0;
		for (int i = 0; i < maxRecordPages; i++)
			if (pages[i].pageId != -1) {
				p++;
				if (p == page)
					return pages[i];
			}

		throw new IllegalArgumentException("Record doesn't use so many pages ("
				+ page + ")");
	}

	public int getPagesUsed() {
		return pagesUsed;
	}
}
