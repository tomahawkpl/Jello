package com.atteo.jello;

import java.io.IOException;

interface PagedFile {
	Page getPage(int id) throws IOException;
	int addPage() throws IOException;
	void removePage();
	void writePage(int id, Page page) throws IOException;
	int getPageCount();
}
