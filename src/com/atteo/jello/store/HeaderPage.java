package com.atteo.jello.store;

import java.util.Arrays;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class HeaderPage extends Page {
	private short pageSize;
	private short blockSize;

	private int freeSpaceInfoPageId;
	private int klassManagerPageId;

	private int fileFormatVersion;
	private final byte[] magic;

	@Inject
	private HeaderPage(@Named("blockSize") final short blockSize,
			@Named("freeSpaceInfoPageId") final int freeSpaceInfoPageId,
			@Named("klassManagerPageId") final int klassManagerPageId,
			@Named("fileFormatVersion") final int fileFormatVersion,
			@Named("magic") final String magic) {
		super();

		this.blockSize = blockSize;
		this.freeSpaceInfoPageId = freeSpaceInfoPageId;
		this.klassManagerPageId = klassManagerPageId;
		this.fileFormatVersion = fileFormatVersion;
		this.magic = magic.getBytes();

		byteBuffer.put(magic.getBytes());
		byteBuffer.putInt(fileFormatVersion);
		byteBuffer.putShort(pageSize);
		byteBuffer.putShort(blockSize);
		byteBuffer.putInt(freeSpaceInfoPageId);
		byteBuffer.putInt(klassManagerPageId);
	}

	public short getBlockSize() {
		return blockSize;
	}

	public int getFileFormatVersion() {
		return fileFormatVersion;
	}

	public int getFreeSpaceInfoPageId() {
		return freeSpaceInfoPageId;
	}

	public int getKlassManagerPageId() {
		return klassManagerPageId;
	}

	public short getPageSize() {
		return pageSize;
	}

	public boolean load() {
		final byte[] readMagic = new byte[magic.length];
		byteBuffer.get(readMagic);
		if (!Arrays.equals(readMagic, magic))
			return false;

		fileFormatVersion = byteBuffer.getInt();
		pageSize = byteBuffer.getShort();
		blockSize = byteBuffer.getShort();
		freeSpaceInfoPageId = byteBuffer.getInt();
		klassManagerPageId = byteBuffer.getInt();

		return true;
	}

}
