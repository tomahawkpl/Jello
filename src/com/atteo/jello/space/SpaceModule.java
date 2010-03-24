package com.atteo.jello.space;

import java.util.HashMap;

import android.util.Pool;
import android.util.Pools;

import com.atteo.jello.OSInfo;
import com.atteo.jello.store.ListPage;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class SpaceModule implements Module {
	// ---- SETTINGS

	private final int appendOnlyCacheSize = 8;
	private final int histogramClasses = 8;
	private final int hybridThreshold = 90;

	private final short blockSize = 128;
	private short blocksPerPage;
	private short freeSpaceInfoSize;
	private short freeSpaceInfosPerPage;
	private short freeSpaceInfoPageCapacity;

	private int maxRecordFragments = 4;
	private int recordPoolLimit = 8;
	// --------------

	private final HashMap<String, String> properties;

	public SpaceModule(final HashMap<String, String> properties) {
		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);
		

	}

	public void configure(final Binder binder) {
		// TODO fix this dependency
		short pageSize = OSInfo.getPageSize();
		
		this.blocksPerPage = (short) (pageSize / blockSize);
		this.freeSpaceInfoSize = (short) (blocksPerPage / (short)Byte.SIZE);
		this.freeSpaceInfoPageCapacity = new ListPage(pageSize).getCapacity();
		this.freeSpaceInfosPerPage = (short) (freeSpaceInfoPageCapacity / freeSpaceInfoSize);

		Names.bindProperties(binder, properties);
		
		binder.bind(AppendOnlyCache.class).to(AppendOnlyCacheNative.class);
		binder.bind(NextFitHistogram.class).to(VanillaHistogram.class);
		
		binder.bind(RecordPoolableManager.class);
	}
	
	@Provides
	Pool<Record> recordPoolProvider(RecordPoolableManager manager, @Named("recordPoolLimit") int limit) {
		return Pools.finitePool(manager, limit);
	}

	private HashMap<String, String> getDefaultProperties() {
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("appendOnlyCacheSize", String.valueOf(appendOnlyCacheSize));
		p.put("histogramClasses", String.valueOf(histogramClasses));
		p.put("hybridThreshold", String.valueOf(hybridThreshold));
		p.put("blockSize", String.valueOf(blockSize));
		p.put("blockPerPage", String.valueOf(blocksPerPage));
		p.put("freeSpaceInfoSize", String.valueOf(freeSpaceInfoSize));
		p.put("freeSpaceInfosPerPage", String.valueOf(freeSpaceInfosPerPage));
		p.put("maxRecordFragments", String.valueOf(maxRecordFragments));
		p.put("recordPoolLimit", String.valueOf(recordPoolLimit));

		
		return p;
	}

}
