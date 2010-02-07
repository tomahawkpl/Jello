package com.atteo.jello.space;

import com.atteo.jello.store.RecordPart;

/**
 * 
 * @author tomahawk
 *
 */
public interface SpaceManagerPolicy {
	public RecordPart[] acquireRecordSpace(int length);
	public RecordPart[] reacquireRecordSpace(RecordPart parts[], int length);
	public void releaseRecordSpace(RecordPart parts[]);
	
	public long acquirePage();
	public void releasePage(long id);
}
