package com.atteo.jello.space;

import android.util.Pool;

import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class Hybrid implements SpaceManagerPolicy {
	private SpaceManager spaceManager;
	private AppendOnlyCache appendOnlyCache;
	private NextFitHistogram nextFitHistogram;
	private PagedFile pagedFile;
	private short threshold;
	private short pageSize;
	private short blockSize;
	private Pool<Record> recordPool;
	
	long currentFreeSpace, currentAverageFreeSpace;
	
	@Inject
	public Hybrid(PagedFile pagedFile, SpaceManager spaceManager,
			AppendOnlyCache appendOnlyCache, NextFitHistogram nextFitHistogram, 
			@Named("hybridThreshold") int hybridThreshold, @Named("pageSize") short pageSize,
			@Named("blockSize") short blockSize, Pool<Record> recordPool) {
		this.spaceManager = spaceManager;
		this.appendOnlyCache = appendOnlyCache;
		this.nextFitHistogram = nextFitHistogram;
		this.pagedFile = pagedFile;
		this.pageSize = pageSize;
		this.blockSize = blockSize;
		this.recordPool = recordPool;
		
		this.threshold = (short) (pageSize * (100-hybridThreshold) / 100);
		
		currentFreeSpace = spaceManager.totalFreeSpace();
		currentAverageFreeSpace = averageFreeSpace();
		
		
	}

	private short averageFreeSpace() {
		int count = pagedFile.getPageCount();
		if (count > 0)
			return (short) (currentFreeSpace / count);
		else
			return 0;
	}
	
	public int acquirePage() {
		if (currentAverageFreeSpace > threshold) {
			int id = appendOnlyCache.getBestId(pageSize);
			if (id != AppendOnlyCache.NO_PAGE) {
				spaceManager.setPageUsed(id, true);
				appendOnlyCache.update(id, (short) 0);
				nextFitHistogram.update(id, pageSize, (short) 0);
				
				currentFreeSpace -= pageSize;
				currentAverageFreeSpace = averageFreeSpace();
				return id;
			}
			
			return acquireNewPage();
			
		} else {
			int id = nextFitHistogram.getWitness(pageSize);
			
			if (id == NextFitHistogram.NO_PAGE) {
				return acquireNewPage();
			}
			
			if (id == NextFitHistogram.NO_WITNESS) {
				int pages = pagedFile.getPageCount();
				for (int i=0;i<pages;i++)
					if (!spaceManager.isPageUsed(i)) {
						id = i;
						break;
					}
						
			}
			
			spaceManager.setPageUsed(id, true);
			appendOnlyCache.update(id, (short) 0);
			nextFitHistogram.update(id, pageSize, (short) 0);
			
			currentFreeSpace -= pageSize;
			currentAverageFreeSpace = averageFreeSpace();
			return id;


		}
	}

	private int acquireNewPage() {
		int id = pagedFile.addPages(1);
		if (id == PagedFile.PAGE_ADD_FAILED)
			return SpaceManagerPolicy.ACQUIRE_FAILED;
			
		spaceManager.update();
		spaceManager.setPageUsed(id, true);
		appendOnlyCache.update(id, (short) 0);
		nextFitHistogram.update(id, (short) -1, (short) 0);
		
		currentFreeSpace -= pageSize;
		currentAverageFreeSpace = averageFreeSpace();
		return id;
	}
	
	public Record acquireRecordSpace(int length) {
		int blocks = getBlocks(length);
		return null;
	}

	public Record reacquireRecordSpace(Record record, int length) {
		// TODO Auto-generated method stub
		return null;
	}

	public void releasePage(int id) {
		spaceManager.setPageUsed(id, false);
		appendOnlyCache.update(id, pageSize);
		nextFitHistogram.update(id, pageSize, (short)0);
		
		currentFreeSpace += pageSize;
		currentAverageFreeSpace = averageFreeSpace();

	}

	public void releaseRecordSpace(Record record) {
		// TODO Auto-generated method stub
	}

	private int getBlocks(int length) {
		return (length % blockSize == 0) ? length / blockSize : length/blockSize + 1;
	}
	
}
