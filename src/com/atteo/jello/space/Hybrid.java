package com.atteo.jello.space;

import com.atteo.jello.Record;
import com.google.inject.Singleton;

@Singleton
public class Hybrid implements SpaceManagerPolicy {

	public int acquirePage() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Record acquireRecord(int length) {
		// TODO Auto-generated method stub
		return null;
	}

	public void reacquireRecord(Record record, int length) {
		// TODO Auto-generated method stub
		return;
	}

	public void releasePage(int id) {
		// TODO Auto-generated method stub
		
	}

	public void releaseRecord(Record record) {
		// TODO Auto-generated method stub
		
	}
/*	private final SpaceManager spaceManager;
	private final AppendOnlyCache appendOnlyCache;
	private final NextFitHistogram nextFitHistogram;
	private final PagedFile pagedFile;
	private final short threshold;
	private final short pageSize;
	private final short blockSize;
	private final int maxRecordSize;
	private final Pool<Record> recordPool;
	private int lastAcquired = 0;

	long currentFreeSpace, currentAverageFreeSpace;

	@Inject
	public Hybrid(final PagedFile pagedFile, final SpaceManager spaceManager,
			final AppendOnlyCache appendOnlyCache,
			final NextFitHistogram nextFitHistogram,
			@Named("hybridThreshold") final int hybridThreshold,
			@Named("pageSize") final short pageSize,
			@Named("blockSize") final short blockSize,
			final Pool<Record> recordPool,
			@Named("maxRecordSize") final int maxRecordSize) {
		this.spaceManager = spaceManager;
		this.appendOnlyCache = appendOnlyCache;
		this.nextFitHistogram = nextFitHistogram;
		this.pagedFile = pagedFile;
		this.pageSize = pageSize;
		this.blockSize = blockSize;
		this.recordPool = recordPool;
		this.maxRecordSize = maxRecordSize;

		
		threshold = (short) (pageSize * (100 - hybridThreshold) / 100);

		currentFreeSpace = spaceManager.totalFreeSpace();
		currentAverageFreeSpace = averageFreeSpace();

	}

	public int acquirePage() {
		final int pages = pagedFile.getPageCount();

		int id;
		
		if (currentAverageFreeSpace > threshold) {
			id = appendOnlyCache.getBestId(pageSize);
			if (id != AppendOnlyCache.NO_PAGE) {
				spaceManager.setPageUsed(id, true);
				appendOnlyCache.update(id, (short) 0);
				nextFitHistogram.update(id, pageSize, (short) 0);

			} else
				id = acquireNewPage();
			
			currentFreeSpace -= pageSize;
			currentAverageFreeSpace = averageFreeSpace();
			lastAcquired = (id + 1) % pages;
			return id;

		} else {
			id = nextFitHistogram.getWitness(pageSize);

			if (id == NextFitHistogram.NO_PAGE) {
				return acquireNewPage();
			}

			if (id == NextFitHistogram.NO_WITNESS)
				for (int i = 0; i < pages; i++)
					if (!spaceManager.isPageUsed((i + lastAcquired) % pages)) {
						id = i;
						break;
					}

			spaceManager.setPageUsed(id, true);
			appendOnlyCache.update(id, (short) 0);
			nextFitHistogram.update(id, pageSize, (short) 0);

			currentFreeSpace -= pageSize;
			currentAverageFreeSpace = averageFreeSpace();
			lastAcquired = (id + 1) % pages;
			return id;
		}
	}

	public Record acquireRecordSpace(final int length) {
		if (length > maxRecordSize)
			throw new IllegalArgumentException("Max record size is " + String.valueOf(maxRecordSize)
					+ " but " + String.valueOf(length) + " was requested");
		final Record result = recordPool.acquire();

		int chunks = length / pageSize; // we we'll satisfy this request using
										// this number of chunks
		if (length % pageSize > 0)
			chunks++;

		int spaceToSpare = chunks * pageSize - length;

		int leftToAcquire = length;
		int reservedOnThisPage;
		
		if (currentAverageFreeSpace > threshold) {
			short freeSpace;
			for (int i = 0; i < chunks; i++) {
				int id = appendOnlyCache.getBestId((short) (pageSize - spaceToSpare));
				
				if (id != AppendOnlyCache.NO_PAGE) {
					freeSpace = appendOnlyCache.getFreeSpace(id);
					reservedOnThisPage = Math.min(leftToAcquire, freeSpace);
					reserveBlocks(result, id, reservedOnThisPage);
					
					appendOnlyCache.update(id, (short) (reservedOnThisPage));
					nextFitHistogram.update(id, (short) freeSpace, (short) reservedOnThisPage);

					spaceToSpare -= pageSize - reservedOnThisPage;
					leftToAcquire -= reservedOnThisPage;
					currentFreeSpace -= reservedOnThisPage;
					lastAcquired = (id + 1) % pageSize;
				} else {
					id = pagedFile.addPages(1);
					if (id == PagedFile.PAGE_ADD_FAILED)
						return null;

					spaceManager.update();
					freeSpace = pageSize;
					reservedOnThisPage = Math.min(leftToAcquire, freeSpace);
					reserveBlocks(result, id, reservedOnThisPage);
					
					appendOnlyCache.update(id, (short) (reservedOnThisPage));
					nextFitHistogram.update(id, (short) freeSpace, (short) reservedOnThisPage);

					spaceToSpare -= pageSize - reservedOnThisPage;
					leftToAcquire -= reservedOnThisPage;
					currentFreeSpace -= reservedOnThisPage;

					currentAverageFreeSpace = averageFreeSpace();
					lastAcquired = 0;
				}	
			}
			
			spaceManager.setAreasUsed(id, areas, used);
		} else {

		}

		return null;
	}
	
	private void reserveBlocks(Record record, int id, int length) {
		short block = 0;
		short start = -1;
		while (length > 0) {
			if (!spaceManager.isBlockUsed(id, block) && start == -1) {
				start = block;
			} else
				if (start != -1) {
					record.setChunkUsed(id, start, block, true);
					start = -1;
				}
		}
		

	}

	public Record reacquireRecordSpace(final Record record, final int length) {
		return null;
	}

	public void releasePage(final int id) {
		spaceManager.setPageUsed(id, false);
		appendOnlyCache.update(id, pageSize);
		nextFitHistogram.update(id, (short) 0, pageSize);

		currentFreeSpace += pageSize;
		currentAverageFreeSpace = averageFreeSpace();

	}

	public void releaseRecordSpace(final Record record) {
		int recordPages = record.getPagesUsed();
		for (int i=0;i<recordPages;i++) {
			PageUsage p = record.getPageUsage(i);
			final short oldSpace = spaceManager.freeSpaceOnPage(p.pageId);

			spaceManager.setAreasUsed(p.pageId, p.usage, false);

			final short newSpace = spaceManager.freeSpaceOnPage(p.pageId);
			appendOnlyCache.update(p.pageId, newSpace);
			nextFitHistogram.update(p.pageId, oldSpace, newSpace);
			currentFreeSpace += newSpace - oldSpace;

		}
		

		currentAverageFreeSpace = averageFreeSpace();
		recordPool.release(record);
		
	}


	public short averageFreeSpace() {
		final int count = pagedFile.getPageCount();
		if (count > 0)
			return (short) (currentFreeSpace / count);
		else
			return 0;
	}

	private int getBlocks(final int length) {
		return length % blockSize == 0 ? length / blockSize : length
				/ blockSize + 1;
	}
*/
}
