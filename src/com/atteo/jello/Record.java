package com.atteo.jello;

import android.util.Poolable;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

/*
 * This class is really messy and should be rewritten
 */
public class Record implements Poolable<Record> {
	private Record nextPoolable;
	private int id;
	private int schemaVersion;
	private final PageUsage[] pages;
	private int pagesUsed;

	@Inject
	static Injector injector;
	@Inject
	static @Named("maxRecordPages")
	int maxRecordPages;

	Record() {
		pages = new PageUsage[maxRecordPages];

		for (int i = 0; i < maxRecordPages; i++)
			pages[i] = new PageUsage();

		pagesUsed = 0;

	}

	public void clearUsage() {
		pagesUsed = 0;
		for (int i = 0; i < maxRecordPages; i++)
			if (pages[i].pageId != -1) {
				pages[i].pageId = -1;
				final int len = pages[i].usage.length;
				final byte[] u = pages[i].usage;
				for (int j = 0; j < len; j++)
					u[j] = 0;
			}
	}

	public int getId() {
		return id;
	}

	public Record getNextPoolable() {
		return nextPoolable;
	}

	public int getPagesUsed() {
		return pagesUsed;
	}

	public PageUsage getPageUsage(final int page) {
		int p = 0;
		for (int i = 0; i < maxRecordPages; i++)
			if (pages[i].pageId != -1) {
				if (p == page)
					return pages[i];
				p++;
			}

		throw new IllegalArgumentException("Record doesn't use so many pages ("
				+ page + ")");
	}

	public int getSchemaVersion() {
		return schemaVersion;
	}

	public void setChunkUsed(final int pageId, final short start,
			final short end, final boolean used) {
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
				pages[index].usage[i / Byte.SIZE] |= 1 << i % Byte.SIZE;
		} else {
			pages[index].blocksUsed -= end - start;
			if (pages[index].blocksUsed == 0) {
				pages[index].pageId = -1;
				pagesUsed--;
			}
			for (int i = start; i < end; i++)
				pages[index].usage[i / Byte.SIZE] &= ~(1 << i % Byte.SIZE);
		}
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void setNextPoolable(final Record element) {
		nextPoolable = element;
	}

	public void setPagesUsed(final int pagesUsed) {
		clearUsage();
		this.pagesUsed = pagesUsed;
		for (int i = 0; i < pagesUsed; i++)
			pages[i].pageId = 0;
	}

	public void setSchemaVersion(final int schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

}
