package com.atteo.jello.store;

import java.io.IOException;


public interface PagedFile {
	int addPage() throws IOException;
	void removePage();
	void writePage(int id, Page page) throws IOException;
	int getPageCount();
	void getPage(int id, Page page) throws IOException;
}
