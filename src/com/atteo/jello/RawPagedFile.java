package com.atteo.jello;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

public class RawPagedFile implements PagedFile {
	@Inject private Injector injector;
	private int pages;
	private File file;
	private RandomAccessFile raf;
	private int pageSize;
	private int fileSizeLimit;
	private boolean readOnly = false;

	@Inject
	public RawPagedFile(@Named("pageSize") int pageSize,
			@Named("fileSizeLimit") int fileSizeLimit, @Assisted File file, @Assisted boolean readOnly) throws IOException {
		
		if (file == null || !file.exists())
			throw new IllegalArgumentException("File argument is null or does not exist");
		
		if (!file.canRead())
			throw new IOException("File is not readable");
		
		if (!readOnly && !file.canWrite())
			readOnly = true;
		
		this.readOnly = readOnly;
		this.file = file;
		this.pageSize = pageSize;
		this.fileSizeLimit = fileSizeLimit;
		raf = new RandomAccessFile(file, "rw");
		long fileLength;
		fileLength = raf.length();
		if (fileLength > fileSizeLimit)
			throw new IOException("Database file " + file.getName()
					+ " too big, current limit " + fileSizeLimit);
		pages = (int)fileLength / pageSize;
		
		if ((int)fileLength % pageSize > 0)
			throw new IllegalArgumentException("Database file corrupted or uses different page size");

		raf.seek(0);
	}
	

	protected void finalize() {
		if (raf != null)
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

	}

	@Override
	public Page getPage(int id) throws IOException {
		checkPageId(id);
		byte data[] = new byte[pageSize];
		raf.seek(pageOffset(id));
		raf.readFully(data, 0, pageSize);
		Page page = injector.getInstance(Page.class);
		page.setData(data);
		return page;
	}

	@Override
	public int getPageCount() {
		return pages;
	}

	@Override
	public int addPage() throws IOException {
		if (readOnly)
			throw new IOException("File opened in read-only mode");
		if ((pages+1) * pageSize > fileSizeLimit)
			return -1;
		try {
			raf.setLength((pages + 1) * pageSize);
		} catch (IOException e) {
			return -1;
		}
		pages++;
		return pages - 1;
	}

	@Override
	public void removePage() {
		if (pages == 0)
			return;
		pages--;
		try {
			raf.setLength(pages * pageSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void writePage(int id, Page page) throws IOException {
		checkPageId(id);
		if (page.getData() == null)
			throw new IllegalArgumentException("Supplied Page argument containts no data");
		raf.seek(pageOffset(id));
		raf.write(page.getData(), 0, pageSize);
		
	}
	
	private int pageOffset(int id) {
		return id * pageSize;
	}
	
	private void checkPageId(int id) {
		if (id >= pages || id < 0)
			throw new IndexOutOfBoundsException("Page id does not exist within file");
	}

	public File getFile() {
		return file;
	}
	
	
	
	public RandomAccessFile getRaf() {
		return raf;
	}


	public boolean isReadOnly() {
		return readOnly;
	}
}
