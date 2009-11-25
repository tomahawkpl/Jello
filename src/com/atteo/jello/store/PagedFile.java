package com.atteo.jello.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.util.Log;

/**
 * @TODO fileSizeLimit, locks
 * @author tomahawk
 * 
 */
public class PagedFile {
	private static int fileDescriptor;
	private static boolean readOnly = false;
	private static File file;
	private static int pageSize;
	private static int pages;
	private static boolean isOpened;

	static {
		System.loadLibrary("PagedFile");
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

		fileDescriptor = openNative(file.getCanonicalPath());

		if (fileDescriptor < 0)
			throw new FileNotFoundException("Unable to open file "
					+ file.getCanonicalPath());
		
		PagedFile.readOnly = readOnly;
		PagedFile.file = file;
		PagedFile.pageSize = getPageSizeNative();
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

	static public int addPage() throws IOException {
		if (readOnly)
			throw new IOException("File opened in read-only mode");

		setLength((pages + 1) * pageSize);

		pages++;
		return pages - 1;
	}

	static public void getPage(final int id, final Page page)
			throws IOException {
		checkPageId(id);
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
		checkPageId(id);
		if (page.getData() == null)
			throw new IllegalArgumentException(
					"Supplied Page argument containts no data");
		writeOnPosition(page.getData(), pageOffset(id), pageSize);

	}

	static private void checkPageId(final int id) {
		if (id >= pages || id < 0)
			throw new IndexOutOfBoundsException(
					"Page id does not exist within file");
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
		Log.i("t","pageSize " + pageSize);
		return pageSize;
	}
	
	native static private int getPageSizeNative();

	native static private void closeNative();

	native static public int length();

	native static private int openNative(String fullpath);

	native static private void readFromPosition(byte[] buffer, int position,
			int length);

	native static private void setLength(int length);

	native static private void writeOnPosition(byte[] buffer, int position,
			int length);
}
