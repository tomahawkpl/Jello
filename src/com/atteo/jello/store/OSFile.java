package com.atteo.jello.store;

import java.io.IOException;

public interface OSFile {
	int length() throws IOException;
	void readFromPosition(byte[] buffer, int position, int length) throws IOException;
	void writeOnPosition(byte[] buffer, int position, int length) throws IOException;
	void setLength(int length) throws IOException;
	void close() throws IOException;
}
