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

		init(pagedFile, proxy, spaceManagerPolicy, freeSpaceInfoSize,
				bTreeLeafCapacity, bTreeNodeCapacity, klassIndexPageId);
	}

	public native void commit();

	public void create() {
		final Page bTreePage = proxy.acquire();
		bTreePage.setId(klassIndexPageId);
		bTreePage.reset();
		bTreePage.putInt(-1);
		pagedFile.writePage(bTreePage);
	}

	public native void debug();

	public native boolean find(Record record);

	public native void insert(Record record);

	public native boolean load();

	public native void remove(int id);

	public native void update(Record record);

	private native void init(PagedFile pagedFile, PagePoolProxy proxy,
			SpaceManagerPolicy spaceManagerPolicy, int freeSpaceInfoSize,
			int bTreeLeafCapacity, int bTreeNodeCapacity, int klassIndexPageId);
}