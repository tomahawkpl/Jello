package com.atteo.jello.store;

import java.util.Arrays;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class HeaderPage extends Page {	
	private short pageSize;
	private short blockSize;
	
	private int freeSpaceMapPageId;
	private int klassManagerPageId;
	
	private int fileFormatVersion;
	private byte[] magic;
	
	@Inject
	private HeaderPage(@Named("blockSize") final short blockSize,
			@Named("freeSpaceMapPageId") int freeSpaceMapPageId,
			@Named("klassManagerPageId") int klassManagerPageId,
			@Named("fileFormatVersion") int fileFormatVersion,
			@Named("magic") String magic) {
		super();
	
		this.blockSize = blockSize;
		this.freeSpaceMapPageId = freeSpaceMapPageId;
		this.klassManagerPageId = klassManagerPageId;
		this.fileFormatVersion = fileFormatVersion;
		this.magic = magic.getBytes();
		
		byteBuffer.put(magic.getBytes());
		byteBuffer.putInt(fileFormatVersion);
		byteBuffer.putShort(pageSize);
		byteBuffer.putShort(blockSize);
		byteBuffer.putInt(freeSpaceMapPageId);
		byteBuffer.putInt(klassManagerPageId);
	}

	public boolean load() {
		byte[] readMagic = new byte[magic.length];
		byteBuffer.get(readMagic);
		if (!Arrays.equals(readMagic, magic))
			return false;
		
		fileFormatVersion = byteBuffer.getInt();
		pageSize = byteBuffer.getShort();
		blockSize = byteBuffer.getShort();
		freeSpaceMapPageId = byteBuffer.getInt();
		klassManagerPageId = byteBuffer.getInt();
		
		return true;
	}
	
	public int getFileFormatVersion() {
		return fileFormatVersion;
	}

	public short getPageSize() {
		return pageSize;
	}
	
	public short getBlockSize() {
		return blockSize;
	}
	
	public int getFreeSpaceMapPageId() {
		return freeSpaceMapPageId;
	}
	
	public int getKlassManagerPageId() {
		return klassManagerPageId;
	}

}
