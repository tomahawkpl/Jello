package com.atteo.jello.space;

import java.io.IOException;
import java.util.ArrayList;

import com.atteo.jello.store.DatabaseFile;
import com.atteo.jello.store.ListPage;
import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.store.RecordPart;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Implements AppendOnly algorithm with a simple cache
 * 
 * @author tomahawk
 * 
 */
@Singleton
public class AppendOnly implements SpaceManagerPolicy {
	private final int pageSize;
	private final int blocksPerPage;
	private final int blockSize;
	private final int freeSpaceInfoSize;
	private final int freeSpaceMapPageCapacity;
	private final int freeSpaceMapPageStart;

	private PagedFile pagedFile;
	private ArrayList<ListPage> freeSpaceMap;
	private Injector injector;
	private AppendOnlyCache appendOnlyCache;

	@Inject
	AppendOnly(Injector inject, PagedFile pagedFile,
			@Named("pageSize") int pageSize, @Named("blockSize") int blockSize,
			@Named("appendOnlyCacheSize") int appendOnlyCacheSize) {
		this.pagedFile = pagedFile;

		this.pageSize = pageSize;
		this.blockSize = blockSize;
		this.blocksPerPage = pageSize / blockSize;
		this.freeSpaceInfoSize = blocksPerPage / Byte.SIZE;

		freeSpaceMap = new ArrayList<ListPage>();
		ListPage l = injector.getInstance(ListPage.class);
		freeSpaceMapPageCapacity = l.getCapacity() / freeSpaceInfoSize;
		freeSpaceMapPageStart = l.getDataStart();

		appendOnlyCache = inject.getInstance(AppendOnlyCache.class);

	}

	/**
	 * Returns a set of RecordPart objects which describe parts of file. Their
	 * total size is equal to or higher than length parameter.
	 */
	public RecordPart[] acquireRecordSpace(int length) {
		ArrayList<RecordPart> result = new ArrayList<RecordPart>();
		while (length > 0) {
			if (!appendOnlyCache.isEmpty()) {
				// There is at least one page with some free space in the cache,
				// we're gonna use it
				long cachedPageId = appendOnlyCache.getBest();
				int freeSpace = appendOnlyCache.getFreeSpace(cachedPageId);
				
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

				appendOnlyCache.update(cachedPageId, freeSpace);

			} else {
				// Page cache is empty, let's allocate a new page and
				// add it to cache
				long id = addPages(1);
				appendOnlyCache.update(id, pageSize);
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
		long prev = -1;
		for (int i = 0; i < parts.length; i++) {
			int startBlock = parts[i].start / blockSize;
			int endBlock = parts[i].end / blockSize;
			for (int j=startBlock;j<endBlock;j++)
				setBlockUnused(parts[i].pageId,j);
			
			if (prev != parts[i].pageId) {
				appendOnlyCache.update(parts[i].pageId, getFreeSpaceOnPage(parts[i].pageId));
				prev = parts[i].pageId;
			}
		}
	}

	/**
	 * 
	 * 
	 * @return page id on success, -1 otherwise
	 */
	public long acquirePage() {
		if (!appendOnlyCache.isEmpty()) {
			long id = appendOnlyCache.getBest();
			if (appendOnlyCache.getFreeSpace(id) == pageSize) {
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

	private long addPages(long count) {
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

	private boolean removePages(long count) {
		try {
			pagedFile.removePages(count);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

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

	public void create() {
		ListPage p = injector.getInstance(ListPage.class);
		p.setId(DatabaseFile.PAGE_FREE_SPACE_MAP);
		freeSpaceMap.add(p);
		pagedFile.writePage(p);
	}

	private int getFreeSpaceOnPage(long id) {
		int freeSpacePage = pageFreeSpaceInfoPageNumber(id);
		int freeSpaceOffset = pageFreeSpaceInfoOffset(id);
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

	private int pageFreeSpaceInfoPageNumber(long id) {
		return (int) (id / freeSpaceMapPageCapacity);
	}

	private int pageFreeSpaceInfoOffset(long id) {
		return freeSpaceMapPageStart + (int) (id % freeSpaceMapPageCapacity);
	}

	private boolean isPageEmpty(long id) {
		return isPageEmpty(pageFreeSpaceInfoPageNumber(id),
				pageFreeSpaceInfoOffset(id));
	}

	private boolean isPageEmpty(long id, int offset) {
		byte[] data = freeSpaceMap.get((int) id).getData();
		for (int i = 0; i < freeSpaceInfoSize; i++)
			if (data[offset + i] != 0)
				return false;
		return true;
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

	private void setPageUsed(long id) {
		ListPage l = freeSpaceMap.get(pageFreeSpaceInfoPageNumber(id));
		int offset = pageFreeSpaceInfoOffset(id);
		byte[] data = l.getData();
		for (int i = 0; i < blockSize; i++)
			data[offset + i] |= 0xFF;
		pagedFile.writePage(l);
	}

	private void setPageUnused(long id) {
		ListPage l = freeSpaceMap.get((int) pageFreeSpaceInfoPageNumber(id));
		int offset = pageFreeSpaceInfoOffset(id);
		byte[] data = l.getData();
		for (int i = 0; i < blockSize; i++)
			data[offset + i] &= 0;

		pagedFile.writePage(l);
	}

	private boolean isBlockUsed(long id, int block) {
		ListPage l = freeSpaceMap.get(pageFreeSpaceInfoPageNumber(id));
		int offset = pageFreeSpaceInfoOffset(id);
		int byteNum = block / Byte.SIZE;
		int byteOffset = block % Byte.SIZE;
		
		return (l.getData()[offset+byteNum] & (1 << byteOffset)) > 0;
	}

	private void setBlockUsed(long id, int block) {
		ListPage l = freeSpaceMap.get(pageFreeSpaceInfoPageNumber(id));
		int offset = pageFreeSpaceInfoOffset(id);
		int byteNum = block / Byte.SIZE;
		int byteOffset = block % Byte.SIZE;
		l.getData()[offset+byteNum] |= (1 << byteOffset);
		
	}

	private void setBlockUnused(long id, int block) {
		ListPage l = freeSpaceMap.get(pageFreeSpaceInfoPageNumber(id));
		int offset = pageFreeSpaceInfoOffset(id);
		int byteNum = block / Byte.SIZE;
		int byteOffset = block % Byte.SIZE;
		l.getData()[offset+byteNum] &= ~(1 << byteOffset);
	}

}
