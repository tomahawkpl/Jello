package com.atteo.jello.space;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SpaceManager {
	@Inject
	private SpaceManager(SpaceManagerPolicy policy) {
		
	}
	
	public boolean load() {
		return true;
	}
	
	public void create() {
		
	}
}
