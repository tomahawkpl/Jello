package com.atteo.jello.index;

import com.atteo.jello.Record;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

public class BTree implements Index {
	static {
		System.loadLibrary("BTree");
	}
	private final int klassIndexPageId;
	private final PagedFile pagedFile;
	private final PagePoolProxy proxy;

	private int btree;
	
	@Inject
	public BTree(final PagedFile pagedFile, final PagePoolProxy proxy,
			final SpaceManagerPolicy spaceManagerPolicy,
			@Named("freeSpaceInfoSize") final int freeSpaceInfoSize,
			@Named("bTreeLeafCapacity") final short bTreeLeafCapacity,
			@Named("bTreeNodeCapacity") final short bTreeNodeCapacity,
			@Assisted final int klassIndexPageId) {
		this.pagedFile = pagedFile;
		this.klassIndexPageId = klassIndexPageId;
		this.proxy = proxy;

		btree = init(pagedFile, proxy, spaceManagerPolicy, freeSpaceInfoSize,
				bTreeLeafCapacity, bTreeNodeCapacity, klassIndexPageId);
	}

	public void create() {
		final Page bTreePage = proxy.acquire();
		bTreePage.setId(klassIndexPageId);
		bTreePage.reset();
		bTreePage.putInt(-1);
		pagedFile.writePage(bTreePage);
	}

	public native void commitNative(int btree);
	
	public native void debugNative(int btree);

	public native boolean findNative(int btree, Record record);

	public native void insertNative(int btree, Record record);

	public native boolean loadNative(int btree);

	public native void removeNative(int btree, int id);

	public native void updateNative(int btree, Record record);

	public native void iterateNative(int btree);
	
	public native int nextIdNative(int btree);
	
	
	public void commit() {
		commitNative(btree);
	}
	
	public void debug() {
		debugNative(btree);
	}

	public boolean find(Record record) {
		return findNative(btree, record);
	}

	public void insert(Record record) {
		insertNative(btree, record);
	}

	public boolean load() {
		return loadNative(btree);
	}

	public void remove(int id) {
		removeNative(btree, id);
	}

	public void update(Record record) {
		updateNative(btree, record);
	}

	public void iterate() {
		iterateNative(btree);
	}
	
	public int nextId() {
		return nextIdNative(btree);
	}
	
	private native int init(PagedFile pagedFile, PagePoolProxy proxy,
			SpaceManagerPolicy spaceManagerPolicy, int freeSpaceInfoSize,
			int bTreeLeafCapacity, int bTreeNodeCapacity, int klassIndexPageId);
}