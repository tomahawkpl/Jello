package com.atteo.jello.space;

import android.util.Pool;

import com.atteo.jello.PageUsage;
import com.atteo.jello.Record;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class AppendOnly implements SpaceManagerPolicy {
	static {
		System.loadLibrary("AppendOnly");
	}

	private short pageSize;
	private short blockSize;
	private int maxRecordSize;

	private AppendOnlyCache appendOnlyCache;
	private SpaceManager spaceManager;
	private PagedFile pagedFile;
	private Pool<Record> recordPool;

	@Inject
	public AppendOnly(AppendOnlyCache appendOnlyCache,
			SpaceManager spaceManager, PagedFile pagedFile,
			@Named("pageSize") short pageSize,
			@Named("blockSize") short blockSize,
			@Named("maxRecordSize") int maxRecordSize, Pool<Record> recordPool) {
		init(appendOnlyCache, spaceManager, pagedFile, pageSize);

		this.pageSize = pageSize;
		this.maxRecordSize = maxRecordSize;
		this.spaceManager = spaceManager;
		this.appendOnlyCache = appendOnlyCache;
		this.pagedFile = pagedFile;
		this.recordPool = recordPool;
		this.blockSize = blockSize;
	}

	private native void init(AppendOnlyCache appendOnlyCache,
			SpaceManager spaceManager, PagedFile pagedFile, short pageSize);

	public native int acquirePage();

	public void acquireRecord(Record record, int length) {
		if (length > maxRecordSize)
			throw new IllegalArgumentException("Max record size is "
					+ String.valueOf(maxRecordSize) + " but "
					+ String.valueOf(length) + " was requested");

		int chunks = length / pageSize; // we we'll satisfy this request using
		// this number of chunks
		if (length % pageSize > 0)
			chunks++;

		int spaceToSpare = chunks * pageSize - length;

		int leftToAcquire = length;
		short reservedOnThisPage;

		short freeSpace;
//		Log.i("jello", "will use " + chunks + " chunks");
		for (int i = 0; i < chunks; i++) {
			int id = appendOnlyCache
					.getBestId((short) (pageSize - spaceToSpare));

			if (id != AppendOnlyCache.NO_PAGE) {
				freeSpace = appendOnlyCache.getFreeSpace(id);
//				Log.i("jello", "page " + id
//						+ " from cache (freespace from cache: " + freeSpace
//						+ ", real freespace: " + spaceManager.freeSpaceOnPage(id)
//						+ ")");
				reservedOnThisPage = (short) Math.min(leftToAcquire, freeSpace);
				short r = reserveBlocks(record, id, reservedOnThisPage);
				
				appendOnlyCache.update(id,
						(short) (freeSpace - r));

				spaceToSpare -= pageSize - reservedOnThisPage;
				leftToAcquire -= reservedOnThisPage;
//				Log.i("jello", leftToAcquire + " bytes left to acquire");

			} else {
				id = pagedFile.addPages(1);
				if (id == PagedFile.PAGE_ADD_FAILED)
					return;

//				Log.i("jello", "new page acquired " + id);

				spaceManager.update();
				freeSpace = pageSize;
				reservedOnThisPage = (short) Math.min(leftToAcquire, freeSpace);
				short r = reserveBlocks(record, id, reservedOnThisPage);

				appendOnlyCache.update(id,
						(short) (freeSpace - r));

				spaceToSpare -= pageSize - reservedOnThisPage;
				leftToAcquire -= reservedOnThisPage;
//				Log.i("jello", leftToAcquire + " bytes left to acquire");
			}
		}
		
//		Log.i("jello","s1:"+spaceManager.freeSpaceOnPage(2));
		spaceManager.setRecordUsed(record, true);
//		Log.i("jello","s1:"+spaceManager.freeSpaceOnPage(2));
	}

	private short reserveBlocks(Record record, int id, short length) {
		short block = 0;
		short start = -1;
		short result = 0;
//		Log.i("jello", "reserving " + length + " bytes on page " + id);
		while (length > 0) {
			if (!spaceManager.isBlockUsed(id, block)) {
				length -= blockSize;
				if (start == -1)
					start = block;
			} else {
				if (start != -1) {
//					Log.i("jello", "chunk from " + start + " to " + block + "("
//							+ (block - start) * blockSize
//							+ " bytes) [next block used]");
					record.setChunkUsed(id, start, block, true);
					result += block - start;
					start = -1;
				}
			}
			block++;
		}

//		Log.i("jello", "chunk from " + start + " to " + block + "("
//				+ (block - start) * blockSize + " bytes) [length = 0]");
		record.setChunkUsed(id, start, block, true);
		result += block - start;
		result *= blockSize;
		return result;
	}

	public native void reacquireRecord(Record record, int length);

	public native void releasePage(int id);

	public void releaseRecord(Record record) {
		int recordPages = record.getPagesUsed();
		short newSpace;
//		Log.i("jello","releasing");
		spaceManager.setRecordUsed(record, false);
		for (int i = 0; i < recordPages; i++) {
			PageUsage p = record.getPageUsage(i);
			newSpace = spaceManager.freeSpaceOnPage(p.pageId);
			appendOnlyCache.update(p.pageId, newSpace);
//			Log.i("jello","page: " + p.pageId +" freespace: " + newSpace);

		}

		recordPool.release(record);
	}
}
