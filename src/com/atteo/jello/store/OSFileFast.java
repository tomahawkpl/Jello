package com.atteo.jello.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.inject.Inject;

/**
 * @TODO locks
 * @author tomahawk
 * 
 */
public class OSFileFast implements OSFile {
	private int fileDescriptor;

	@Inject
	OSFileFast(File file) throws IOException {
		System.loadLibrary("OSFileFast");
		fileDescriptor = openNative(file.getCanonicalPath());

		if (fileDescriptor < 0)
			throw new FileNotFoundException("Unable to open file "
					+ file.getCanonicalPath());
	}

	native private int openNative(String fullpath);

	native private int lengthNative(int fileDescriptor);

	private void checkOpened() throws IOException {
		if (fileDescriptor < 0)
			throw new IOException("Invalid file descriptor");
	}

	@Override
	public int length() throws IOException {
		checkOpened();
		return lengthNative(fileDescriptor);
	}

	native private void readFromPositionNative(int fileDescriptor,
			byte[] buffer, int position, int length);

	@Override
	public void readFromPosition(byte[] buffer, int position, int length)
			throws IOException {
		checkOpened();
		readFromPositionNative(fileDescriptor, buffer, position, length);
	}

	native private void writeOnPositionNative(int fileDescriptor,
			byte[] buffer, int position, int length);

	@Override
	public void writeOnPosition(byte[] buffer, int position, int length)
			throws IOException {
		checkOpened();
		writeOnPositionNative(fileDescriptor, buffer, position, length);
	}

	native private void closeNative(int fileDescriptor);
	
	@Override
	public void close() throws IOException {
		checkOpened();
		closeNative(fileDescriptor);
	}

	native private void setLengthNative(int fileDescriptor, int length);
	
	@Override
	public void setLength(int length) throws IOException {
		checkOpened();
		setLengthNative(fileDescriptor, length);

	}
}
