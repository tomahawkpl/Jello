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
	private int klassIndexPageId;
	private PagedFile pagedFile;
	private PagePoolProxy proxy;
	
	@Inject
	public BTree(PagedFile pagedFile, PagePoolProxy proxy,
			SpaceManagerPolicy spaceManagerPolicy,
			@Named("freeSpaceInfoSize") int freeSpaceInfoSize,
			@Named("bTreeLeafCapacity") short bTreeLeafCapacity,
			@Named("bTreeNodeCapacity") short bTreeNodeCapacity,
			@Assisted int klassIndexPageId) {
		this.pagedFile = pagedFile;
		this.klassIndexPageId = klassIndexPageId;
		this.proxy = proxy;
		
		init(pagedFile, proxy, spaceManagerPolicy, freeSpaceInfoSize,
				bTreeLeafCapacity, bTreeNodeCapacity, klassIndexPageId);
	}

	private native void init(PagedFile pagedFile, PagePoolProxy proxy,
			SpaceManagerPolicy spaceManagerPolicy, int freeSpaceInfoSize,
			int bTreeLeafCapacity, int bTreeNodeCapacity, int klassIndexPageId);

	public void create() {
		Page bTreePage = proxy.acquire();
		bTreePage.setId(klassIndexPageId);
		bTreePage.reset();
		bTreePage.putInt(-1);
		pagedFile.writePage(bTreePage);
	}

	public native boolean load();

	public native void commit();
	
	public native void remove(int id);

	public native void insert(Record record);

	public native boolean find(Record record);

	public native void update(Record record);

	public native void debug();
}