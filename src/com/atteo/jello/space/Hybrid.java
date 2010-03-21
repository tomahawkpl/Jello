package com.atteo.jello.space;

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
	
	long currentFreeSpace, currentAverageFreeSpace;
	
	@Inject
	public Hybrid(PagedFile pagedFile, SpaceManager spaceManager,
			AppendOnlyCache appendOnlyCache, NextFitHistogram nextFitHistogram, 
			@Named("hybridThreshold") int hybridThreshold, @Named("pageSize") short pageSize,
			@Named("blockSize") short blockSize) {
		this.spaceManager = spaceManager;
		this.appendOnlyCache = appendOnlyCache;
		this.nextFitHistogram = nextFitHistogram;
		this.pagedFile = pagedFile;
		this.pageSize = pageSize;
		this.blockSize = blockSize;
		
		this.threshold = (short) (pageSize * (100-hybridThreshold) / 100);
		
		currentFreeSpace = spaceManager.totalFreeSpace();
		currentAverageFreeSpace = averageFreeSpace();
		
		
	}

	private short averageFreeSpace() {
		return (short) (currentFreeSpace / pagedFile.getPageCount());
	}
	
	public int acquirePage() {
		//if (currentAverageFreeSpace > threshold) {
			int id = appendOnlyCache.getBestId(pageSize);
			if (id != AppendOnlyCache.NO_PAGE) {
				spaceManager.setPageUsed(id, true);
				appendOnlyCache.update(id, (short) 0);
				nextFitHistogram.update(id, pageSize, (short) 0);
				
				currentFreeSpace -= pageSize;
				currentAverageFreeSpace = averageFreeSpace();
				return id;
			}
			
			id = pagedFile.addPages(1);
			if (id == PagedFile.PAGE_ADD_FAILED)
				return SpaceManagerPolicy.ACQUIRE_FAILED;
				
			spaceManager.update();
			spaceManager.setPageUsed(id, true);
			appendOnlyCache.update(id, (short) 0);
			nextFitHistogram.update(id, (short) -1, (short) 0);
			
			currentFreeSpace -= pageSize;
			currentAverageFreeSpace = averageFreeSpace();
			return id;
			
		//} else {
			
		//}
		//return 0;
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
