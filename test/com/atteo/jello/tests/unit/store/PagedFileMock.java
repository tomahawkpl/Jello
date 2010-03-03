package com.atteo.jello.tests.unit.store;

import java.io.IOException;
import java.util.ArrayList;

import com.atteo.jello.Jello;
import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagedFile;

public class PagedFileMock implements PagedFile {

	private final int pageSize;

	private ArrayList<Page> pages;

	public PagedFileMock(int pageSize) {
		this.pageSize = pageSize;
		pages = new ArrayList<Page>();
	}

	public long addPages(long count) throws IOException {
		for (int i = 0; i < count; i++)
			pages.add(new Page(pageSize));
		return pages.size() - 1;
	}

	public void close() throws IOException {

	}

	public long getFileLength() {
		return pages.size() * pageSize;
	}

	public long getPageCount() {
		return pages.size();
	}

	public boolean isReadOnly() {
		return false;
	}

	public int open() throws IOException {
		return Jello.OPEN_SUCCESS;
	}

	public void readPage(Page page) {
		System.arraycopy(pages.get((int)page.getId()).getData(), 0, page.getData(), 0, pageSize);

	}

	public void removePages(long count) throws IOException {
		for (int i = 0; i < count; i++)
			pages.remove(pages.size() - 1);

	}

	public void syncAll() {

	}

	public void syncPages(long startPageId, long count) {

	}

	public void writePage(Page page) {
		System.arraycopy(page.getData(), 0, pages.get((int)page.getId()).getData(), 0, pageSize);
	}

}
