package com.atteo.jello.store;

import java.util.HashMap;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class StoreModule implements Module {
	// ---- SETTINGS
	private final int pageSize = OSInfo.getPageSize();
	private final int pagePoolLimit = 5;
	private final String fullpath;

	private final int blockSize = 128;
	private final int blocksPerPage;
	private final int freeSpaceInfoSize;
	private final int freeSpaceInfosPerPage;
	private final int freeSpaceInfoPageCapacity;
	// --------------

	private final HashMap<String, String> properties;

	public StoreModule(final String fullpath, final HashMap<String, String> properties) {
		this.fullpath = fullpath;
		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);
		
		this.blocksPerPage = pageSize / blockSize;
		this.freeSpaceInfoSize = blocksPerPage / Byte.SIZE;
		this.freeSpaceInfoPageCapacity = new ListPage(pageSize).getCapacity();
		this.freeSpaceInfosPerPage = freeSpaceInfoPageCapacity / freeSpaceInfoSize;

	}

	public void configure(final Binder binder) {
		Names.bindProperties(binder, properties);
		binder.bind(PagePool.class);
		binder.bind(HeaderPage.class);
		binder.bind(DatabaseFile.class);
		binder.bind(PagedFile.class).to(PagedFileRAF.class);
	}

	private HashMap<String, String> getDefaultProperties() {
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("pagePoolLimit", String.valueOf(pagePoolLimit));
		p.put("pageSize", String.valueOf(pageSize));
		p.put("fullpath",fullpath);
		p.put("blockSize", String.valueOf(blockSize));
		p.put("blockPerPage", String.valueOf(blocksPerPage));
		p.put("freeSpaceInfoSize", String.valueOf(freeSpaceInfoSize));
		p.put("freeSpaceInfosPerPage", String.valueOf(freeSpaceInfosPerPage));

		return p;
	}

}
