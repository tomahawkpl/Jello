package com.atteo.jello.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class OSFileRAF implements OSFile {
	private final RandomAccessFile raf;

	@Inject
	OSFileRAF(@Assisted final File file) throws FileNotFoundException {
		raf = new RandomAccessFile(file, "rw");
	}

	public void close() throws IOException {
		raf.close();
	}

	public int length() throws IOException {
		return (int) raf.length();

	}

	public void readFromPosition(final byte[] buffer, final int position,
			final int length) throws IOException {
		raf.seek(position);
		raf.readFully(buffer, 0, length);

	}

	public void setLength(final int length) throws IOException {
		raf.setLength(length);
	}

	public void writeOnPosition(final byte[] buffer, final int position,
			final int length) {
		// TODO Auto-generated method stub

	}

}
