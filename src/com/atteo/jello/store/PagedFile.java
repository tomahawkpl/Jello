package com.atteo.jello.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PagedFile {
	private static int fileDescriptor;
	private static boolean readOnly = false;
	private static File file;
	private static int pageSize;
	private static int pages;
	private static boolean isOpened;

	static {
		System.loadLibrary("PagedFile");
		PagedFile.pageSize = getPageSizeNative();
	}
	
	public static void open(final File file, boolean readOnly)
			throws IOException {
		if (PagedFile.isOpened)
			throw new RuntimeException("File already opened, close first");

		if (file == null || !file.exists())
			throw new IllegalArgumentException(
					"File argument is null or does not exist");

		if (!file.canRead())
			throw new IOException("File is not readable");

		if (!readOnly && !file.canWrite())
			readOnly = true;

		fileDescriptor = openNative(file.getCanonicalPath(), readOnly ? 1 : 0);

		if (fileDescriptor < 0)
			throw new FileNotFoundException("Unable to open file "
					+ file.getCanonicalPath());

		PagedFile.readOnly = readOnly;
		PagedFile.file = file;
		
		long fileLength;
		fileLength = PagedFile.length();
		pages = (int) fileLength / pageSize;
		PagedFile.isOpened = true;


	}

	static public void close() {
		if (isOpened) {
			closeNative();
			isOpened = false;
		}
	}

	static public int addPages(int count) throws IOException {
		if (readOnly)
			throw new IOException("File opened in read-only mode");
		setLength((pages + count) * pageSize);

		pages += count;
		return pages - 1;
	}

	static public void getPage(final int id, final Page page)
			throws IOException {
		readFromPosition(page.getData(), pageOffset(id), pageSize);
	}

	static public void removePage() {
		if (pages == 0)
			return;
		pages--;
		setLength(pages * pageSize);

	}

	static public void writePage(final int id, final Page page)
			throws IOException {
		writeOnPosition(page.getData(), pageOffset(id), pageSize);
	}

	public static boolean isOpened() {
		return isOpened;
	}

	static public int getPageCount() {
		return PagedFile.pages;
	}

	static public boolean isReadOnly() {
		return PagedFile.readOnly;
	}

	static public File getFile() {
		return PagedFile.file;
	}

	static private int pageOffset(int id) {
		return id * pageSize;
	}

	static public int getPageSize() {
		return pageSize;
	}
	
	static public void syncPage(int page) {
		syncArea(page * pageSize, pageSize);
	}
	
	static public void syncAll() {
		syncArea(0, length());
	}
	
	native static private void syncArea(int position, int length);
	
	native static private int getPageSizeNative();

	native static private void closeNative();

	native static public int length();

	native static private int openNative(String fullpath, int ro) throws IOException;

	native static private void readFromPosition(byte[] buffer, int position,
			int length);

	native static private void setLength(int length);

	native static private void writeOnPosition(byte[] buffer, int position,
			int length);
}
