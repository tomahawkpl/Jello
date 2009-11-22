package com.atteo.jello.store;

import java.io.File;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

public class RawPagedFile implements PagedFile {
	private final File file;
	private final int fileSizeLimit;
	private final OSFile osFile;
	private int pages;
	private final int pageSize;
	private boolean readOnly = false;

	@Inject
	public RawPagedFile(final OSFileFactory osFileFactory,
			@Named("pageSize") final int pageSize,
			@Named("fileSizeLimit") final int fileSizeLimit,
			@Assisted final File file, @Assisted boolean readOnly)
			throws IOException {

		if (file == null || !file.exists())
			throw new IllegalArgumentException(
					"File argument is null or does not exist");

		if (!file.canRead())
			throw new IOException("File is not readable");

		if (!readOnly && !file.canWrite())
			readOnly = true;

		this.readOnly = readOnly;
		this.file = file;
		this.pageSize = pageSize;
		this.fileSizeLimit = fileSizeLimit;
		osFile = osFileFactory.create(file);
		long fileLength;
		fileLength = osFile.length();
		if (fileLength > fileSizeLimit)
			throw new IOException("Database file " + file.getName()
					+ " too big, current limit " + fileSizeLimit);
		pages = (int) fileLength / pageSize;

		if ((int) fileLength % pageSize > 0)
			throw new IllegalArgumentException(
					"Database file corrupted or uses different page size");

	}

	
	public int addPage() throws IOException {
		if (readOnly)
			throw new IOException("File opened in read-only mode");
		if ((pages + 1) * pageSize > fileSizeLimit)
			return -1;
		try {
			osFile.setLength((pages + 1) * pageSize);
		} catch (final IOException e) {
			return -1;
		}
		pages++;
		return pages - 1;
	}

	public File getFile() {
		return file;
	}

	public OSFile getOSFile() {
		return osFile;
	}

	
	public void getPage(final int id, final Page page) throws IOException {
		checkPageId(id);
		osFile.readFromPosition(page.getData(), pageOffset(id), pageSize);
	}

	
	public int getPageCount() {
		return pages;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	
	public void removePage() {
		if (pages == 0)
			return;
		pages--;
		try {
			osFile.setLength(pages * pageSize);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	
	public void writePage(final int id, final Page page) throws IOException {
		checkPageId(id);
		if (page.getData() == null)
			throw new IllegalArgumentException(
					"Supplied Page argument containts no data");
		osFile.writeOnPosition(page.getData(), pageOffset(id), pageSize);

	}

	private void checkPageId(final int id) {
		if (id >= pages || id < 0)
			throw new IndexOutOfBoundsException(
					"Page id does not exist within file");
	}

	private int pageOffset(final int id) {
		return id * pageSize;
	}

	protected void finalize() {
		if (osFile != null)
			try {
				osFile.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}

	}
}
