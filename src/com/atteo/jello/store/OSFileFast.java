package com.atteo.jello.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @TODO locks
 * @author tomahawk
 * 
 */
public class OSFileFast implements OSFile {
	private final int fileDescriptor;

	@Inject
	OSFileFast(@Assisted final File file) throws IOException {
		System.loadLibrary("OSFileFast");
		fileDescriptor = openNative(file.getCanonicalPath());

		if (fileDescriptor < 0)
			throw new FileNotFoundException("Unable to open file "
					+ file.getCanonicalPath());
	}

	
	public void close() throws IOException {
		checkOpened();
		closeNative(fileDescriptor);
	}

	
	public int length() throws IOException {
		checkOpened();
		return lengthNative(fileDescriptor);
	}

	
	public void readFromPosition(final byte[] buffer, final int position,
			final int length) throws IOException {
		checkOpened();
		readFromPositionNative(fileDescriptor, buffer, position, length);
	}

	
	public void setLength(final int length) throws IOException {
		checkOpened();
		setLengthNative(fileDescriptor, length);

	}

	
	public void writeOnPosition(final byte[] buffer, final int position,
			final int length) throws IOException {
		checkOpened();
		writeOnPositionNative(fileDescriptor, buffer, position, length);
	}

	private void checkOpened() throws IOException {
		if (fileDescriptor < 0)
			throw new IOException("Invalid file descriptor");
	}

	native private void closeNative(int fileDescriptor);

	native private int lengthNative(int fileDescriptor);

	native private int openNative(String fullpath);

	native private void readFromPositionNative(int fileDescriptor,
			byte[] buffer, int position, int length);

	native private void setLengthNative(int fileDescriptor, int length);

	native private void writeOnPositionNative(int fileDescriptor,
			byte[] buffer, int position, int length);
}
