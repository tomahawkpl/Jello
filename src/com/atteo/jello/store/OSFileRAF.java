package com.atteo.jello.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class OSFileRAF implements OSFile {
	private RandomAccessFile raf;
	
	@Inject
	OSFileRAF(@Assisted File file) throws FileNotFoundException {
		raf = new RandomAccessFile(file, "rw");
	}
	
	@Override
	public int length() throws IOException {
		return (int) raf.length();

	}

	@Override
	public void readFromPosition(byte[] buffer, int position, int length) throws IOException {
		raf.seek(position);
		raf.readFully(buffer, 0, length);
		
	}

	@Override
	public void writeOnPosition(byte[] buffer, int position, int length) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws IOException {
		raf.close();
	}

	@Override
	public void setLength(int length) throws IOException {
		raf.setLength(length);
	}

}
