package com.atteo.jello.tests.unit.store;

import java.util.ArrayList;

import com.atteo.jello.Jello;
import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class PagedFileMock implements PagedFile {

	private final short pageSize;

	private ArrayList<Page> pages;

	@Inject
	public PagedFileMock(@Named("pageSize") final short pageSize) {
		this.pageSize = pageSize;
	}

	public int addPages(final int count) {
		for (int i = 0; i < count; i++)
			pages.add(new Page(pageSize));
		return pages.size() - 1;
	}

	public void close() {

	}

	public long getFileLength() {
		if (pages != null)
			return pages.size() * pageSize;
		else
			return 0;
	}

	public int getPageCount() {
		if (pages != null)
			return pages.size();
		else
			return 0;
	}

	public boolean isReadOnly() {
		return false;
	}

	public int open() {
		return Jello.OPEN_SUCCESS;
	}

	public void readPage(final Page page) {
		System.arraycopy(pages.get(page.getId()).getData(), 0, page.getData(),
				0, pageSize);
	}

	public void removePages(int count) {
		if (count > pages.size())
			count = pages.size();
		for (int i = 0; i < count; i++)
			pages.remove(pages.size() - 1);

	}

	public void syncAll() {

	}

	public void syncPages(final int startPageId, final int count) {

	}

	public void writePage(final Page page) {
		System.arraycopy(page.getData(), 0, pages.get(page.getId()).getData(),
				0, pageSize);
	}

	public boolean create() {
		pages = new ArrayList<Page>();
		return true;
	}

	public void remove() {
		pages = null;
	}

	public boolean exists() {
		return pages != null;
	}

}
