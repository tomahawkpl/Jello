package com.atteo.jello;

import java.util.HashMap;

import com.atteo.jello.space.SpaceModule;
import com.atteo.jello.store.StoreModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class JelloModule implements Module {
	// ---- SETTINGS
	private final int fileFormatVersion = 0;
	private final String magic = "JelloDatabase";
	
	private int headerPageId = 0;
	private int freeSpaceMapPageId = 1;
	private int klassManagerPageId = 2;
	private int minimumPages;
	// --------------

	private final String fullpath;

	private final HashMap<String, String> properties;

	public JelloModule(	final String fullpath, final HashMap<String, String> properties) {
		this.fullpath = fullpath;
		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);
		
		headerPageId = Integer.valueOf(properties.get("headerPageId"));
		freeSpaceMapPageId = Integer.valueOf(properties.get("freeSpaceMapPageId"));
		klassManagerPageId = Integer.valueOf(properties.get("klassManagerPageId"));
		
		minimumPages = Math.min(freeSpaceMapPageId, klassManagerPageId);
		minimumPages++;
		
		properties.put("minimumPages", String.valueOf(minimumPages));
	}

	public void configure(final Binder binder) {
		binder.install(new StoreModule(fullpath, null));
		binder.install(new SpaceModule(null));
		
		Names.bindProperties(binder, properties);
	}
	
	private HashMap<String, String> getDefaultProperties() {
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("fileFormatVersion", String.valueOf(fileFormatVersion));
		p.put("magic", String.valueOf(magic));

		p.put("headerPageId", String.valueOf(headerPageId));
		p.put("freeSpaceMapPageId", String.valueOf(freeSpaceMapPageId));
		p.put("klassManagerPageId", String.valueOf(klassManagerPageId));
		p.put("minimumPages", String.valueOf(minimumPages));
		return p;
	}



}
