package com.atteo.jello.store;

import java.io.IOException;
import java.util.ArrayList;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class SpaceManager {
	private final int blockSize;

	private final int freeSpaceInfoSize;
	private final int pageSize;
	private final int freeSpaceMapPageCapacity;

	private PagedFile pagedFile;
	private Injector injector;
	
	private ArrayList<ListPage> freeSpaceMap;

	private final int freeSpaceMapPageStart;
	private final int blocksPerPage;
	
	private final AppendOnlyCache appendOnlyCache;
	
	//static {
	//	System.loadLibrary("SpaceManager");
	//}

	@Inject
	public SpaceManager(PagedFile pagedFile,
			@Named("blockSize") int blockSize, @Named("freeSpaceInfoSize") int freeSpaceInfoSize,
			@Named("pageSize") int pageSize, @Named("blocksPerPage") int blocksPerPage,
			AppendOnlyCache appendOnlyCache) {
		this.pagedFile = pagedFile;

		this.blocksPerPage = blocksPerPage;
		this.blockSize = blockSize;
		this.freeSpaceInfoSize = freeSpaceInfoSize;
		this.freeSpaceMapPageStart = ListPage.getDataStart();
		this.freeSpaceMapPageCapacity = pageSize - freeSpaceMapPageStart;
		this.pageSize = pageSize;
		
		this.appendOnlyCache = appendOnlyCache;
		
		//initVariables(this);
		
		
	}
	
	//private native void initVariables(SpaceManager spaceManager);
	//public native void create(long spaceStart);
	
	public boolean load() {
		long s = pagedFile.getPageCount();

		long next = DatabaseFile.PAGE_FREE_SPACE_MAP;
		int i = 0;
		ListPage p;
		while (next != 0) {
			if (next >= s || next < 0)
				return false;
			p = injector.getInstance(ListPage.class);
			p.setId(next);
			freeSpaceMap.add(p);
			pagedFile.readPage(p);
			next = freeSpaceMap.get(i).getNext();
			i++;
		}

		return true;
	}

	
	public void create(long spaceStart) {
		ListPage p = injector.getInstance(ListPage.class);
		p.setId(DatabaseFile.PAGE_FREE_SPACE_MAP);
		p.setNext(0);
		freeSpaceMap.add(p);
		for (long i=0;i<spaceStart;i++)
			setPageUsed(i);
		pagedFile.writePage(p);
	}
	
	
	/**
	 * Returns a set of RecordPart objects which describe parts of file. Their
	 * total size is equal to or higher than length parameter.
	 */
	//public synchronized native RecordPart[] acquireRecordSpace(int length);
	
	//public synchronized native long acquirePage();
	
	
	public RecordPart[] acquireRecordSpace(int length) {
		ArrayList<RecordPart> result = new ArrayList<RecordPart>();
		while (length > 0) {
			if (!appendOnlyCache.isEmpty()) {
				// There is at least one page with some free space in the cache,
				// we're gonna use it
				long cachedPageId = appendOnlyCache.getBestId();
				int freeSpace = appendOnlyCache.getBestFreeSpace();
				
				RecordPart rp = null;

				for (int i = 0; i < blocksPerPage; i++) {
					if (!isBlockUsed(cachedPageId, i)) {
						if (rp == null) {
							rp = new RecordPart();
							rp.pageId = cachedPageId;
							rp.start = i * blockSize;
							rp.end = i * blockSize;
						}

						rp.end += blockSize;
						length -= blockSize;
						
						setBlockUsed(cachedPageId,i);
						
						freeSpace -= blockSize;
						
						if (length <= 0) {
							result.add(rp);
							rp = null;
							break;
						}

					} else if (rp != null) {
						result.add(rp);
						rp = null;
					}

				}

				if (rp != null) {
					result.add(rp);
				}


			} else {
				// Page cache is empty, let's allocate a new page and
				// add it to cache
				long id = addPages(1);
			}
		}
		return result.toArray(new RecordPart[result.size()]);
	}

	 

	public RecordPart[] reacquireRecordSpace(RecordPart parts[], int length) {
		// Probably could be sped up by a full implementation
		releaseRecordSpace(parts);
		return acquireRecordSpace(length);
	}

	public void releaseRecordSpace(RecordPart parts[]) {
		for (int i = 0; i < parts.length; i++) {
			int startBlock = parts[i].start / blockSize;
			int endBlock = parts[i].end / blockSize;
			for (int j=startBlock;j<endBlock;j++)
				setBlockUnused(parts[i].pageId,j);
			
		}
	}

	/**
	 * 
	 * 
	 * @return page id on success, -1 otherwise
	 */
	public long acquirePage() {
		if (!appendOnlyCache.isEmpty()) {
			long id = appendOnlyCache.getBestId();
			if (appendOnlyCache.getBestFreeSpace() == pageSize) {
					setPageUsed(id);
					appendOnlyCache.update(id, 0);
					return id;
			}
		}
		long id = addPages(1);
		setPageUsed(id);
		return id;
	}

	public void releasePage(long id) {
		setPageUnused(id);
		long last = pagedFile.getPageCount() - 1;
		appendOnlyCache.update(last, pageSize);
		
		long toRemove = 0;
		while (isPageEmpty(last)) {
			toRemove++;
			appendOnlyCache.update(last, 0);
			last--;
		}
		removePages(toRemove);
	}



	public long addPages(long count) {
		long r;
		try {
			r = pagedFile.addPages(count);
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}

		if (!createFreeSpaceMapFor(r))
			return -1;

		for (int i = 0; i < count; i++)
			setPageUnused(r - i);

		return r;
	}

	public boolean removePages(long count) {
		try {
			pagedFile.removePages(count);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		// TODO: remove freeSpaceInfo if possible
		return true;
	}

	public int getFreeSpaceOnPage(long id) {
		int freeSpacePage = freeSpaceInfoPageNumber(id);
		int freeSpaceOffset = freeSpaceInfoOffset(id);
		int result = 0;
		Page page = freeSpaceMap.get(freeSpacePage);
		byte[] data = page.getData();
		for (int i = freeSpaceOffset; i < freeSpaceOffset + freeSpaceInfoSize; i++)
			for (int j = 0; j < Byte.SIZE; j++)
				if ((data[i] & (1 << j)) == 0)
					result++;
		result *= blockSize;
		return result;
	}


	private int freeSpaceInfoPageNumber(long id) {
		return (int) (id / (freeSpaceMapPageCapacity / freeSpaceInfoSize));
	}

	private int freeSpaceInfoOffset(long id) {
		return freeSpaceMapPageStart + (int) (id % (freeSpaceMapPageCapacity / freeSpaceInfoSize));
	}


	private boolean createFreeSpaceMapFor(long id) {
		long newPageId;
		ListPage last = freeSpaceMap.get(freeSpaceMap.size() - 1);
		boolean changed = false;
		while (id > freeSpaceMapPageCapacity * freeSpaceMap.size() - 1) {
			try {
				newPageId = pagedFile.addPages(1);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}

			last.setNext(newPageId);
			pagedFile.writePage(last);
			last = injector.getInstance(ListPage.class);
			last.setId(newPageId);
			freeSpaceMap.add(last);
			changed = true;
		}

		if (changed)
			pagedFile.writePage(last);
		return true;
	}

	public boolean isPageEmpty(long id) {
		return isPageEmpty(freeSpaceInfoPageNumber(id),
				freeSpaceInfoOffset(id));
	}

	public boolean isPageEmpty(long id, int offset) {
		byte[] data = freeSpaceMap.get((int) id).getData();
		for (int i = 0; i < freeSpaceInfoSize; i++)
			if (data[offset + i] != 0)
				return false;
		return true;
	}

	public void setPageUsed(long id) {
		ListPage l = freeSpaceMap.get(freeSpaceInfoPageNumber(id));
		int offset = freeSpaceInfoOffset(id);
		byte[] data = l.getData();
		for (int i = 0; i < freeSpaceInfoSize; i++)
			data[offset + i] = (byte) 0xFF;
		pagedFile.writePage(l);
		
	}

	public void setPageUnused(long id) {
		ListPage l = freeSpaceMap.get((int) freeSpaceInfoPageNumber(id));
		int offset = freeSpaceInfoOffset(id);
		byte[] data = l.getData();
		for (int i = 0; i < freeSpaceInfoSize; i++)
			data[offset + i] = 0;

		pagedFile.writePage(l);
		
	}

	public boolean isBlockUsed(long id, int block) {
		ListPage l = freeSpaceMap.get(freeSpaceInfoPageNumber(id));
		int offset = freeSpaceInfoOffset(id);
		int byteNum = block / Byte.SIZE;
		int byteOffset = block % Byte.SIZE;
		return (l.getData()[offset + byteNum] & (1 << byteOffset)) > 0;
	}

	public void setBlockUsed(long id, int block) {
		ListPage l = freeSpaceMap.get(freeSpaceInfoPageNumber(id));
		int offset = freeSpaceInfoOffset(id);
		int byteNum = block / Byte.SIZE;
		int byteOffset = block % Byte.SIZE;
		byte data[] = l.getData();
		data[offset + byteNum] |= (1 << byteOffset);
		int freeSpace = 0;
		for (int i=0;i<freeSpaceInfoSize;i++)
			for (int j=0;j<Byte.SIZE;j++)
				if ((data[offset + i] & 0xFF) == 0)
					freeSpace++;
		
		
	}

	public void setBlockUnused(long id, int block) {
		ListPage l = freeSpaceMap.get(freeSpaceInfoPageNumber(id));
		int offset = freeSpaceInfoOffset(id);
		int byteNum = block / Byte.SIZE;
		int byteOffset = block % Byte.SIZE;
		byte data[] = l.getData();
		data[offset + byteNum] &= ~(1 << byteOffset);
		
		int freeSpace = 0;
		for (int i=0;i<freeSpaceInfoSize;i++)
			for (int j=0;j<Byte.SIZE;j++)
				if ((data[offset + i] & 0xFF) == 0)
					freeSpace++;
		

	}
}
