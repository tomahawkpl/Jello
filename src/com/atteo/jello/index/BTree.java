package com.atteo.jello.index;

import com.atteo.jello.Record;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

public class BTree implements Index {
	static {
		System.loadLibrary("BTree");
	}

	@Inject
	public BTree(PagedFile pagedFile,
			SpaceManagerPolicy spaceManagerPolicy,
			@Named("freeSpaceInfoSize") int freeSpaceInfoSize,
			@Named("bTreeLeafCapacity") short bTreeLeafCapacity,
			@Named("bTreeNodeCapacity") short bTreeNodeCapacity,
			@Assisted int klassIndexPageId) {
		init(freeSpaceInfoSize, bTreeLeafCapacity,
				bTreeNodeCapacity);
	}

	private native void init(int freeSpaceInfoSize,
			short bTreeLeafCapacity, short bTreeNodeCapacity);

	public native void create(int pageId);

	public native void load(int pageId);

	public native void remove(int id);

	public native void insert(Record record);

	public native boolean find(Record record);

	public native void update(Record record);
}