package com.atteo.jello.space;

import java.util.HashMap;

import android.util.Pool;
import android.util.Pools;

import com.atteo.jello.Record;
import com.atteo.jello.RecordPoolableManager;
import com.atteo.jello.store.ListPage;
import com.atteo.jello.store.PageSizeProvider;
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

	private final int maxRecordPages = 4;
	private final int maxRecordSize;
	private final int recordPoolLimit = 8;
	private final int chunkPoolLimit = 8;
	// --------------

	private final short pageSize;
	
	private final HashMap<String, String> properties;

	public SpaceModule(final HashMap<String, String> properties) {
		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);
		
		pageSize = new PageSizeProvider().get();
		maxRecordSize = maxRecordPages * pageSize;
		
	}

	public void configure(final Binder binder) {
		blocksPerPage = (short) (pageSize / blockSize);
		freeSpaceInfoSize = (short) (blocksPerPage / (short) Byte.SIZE);
		freeSpaceInfoPageCapacity = new ListPage(pageSize).getCapacity();
		freeSpaceInfosPerPage = (short) (freeSpaceInfoPageCapacity / freeSpaceInfoSize);

		Names.bindProperties(binder, properties);

		binder.bind(AppendOnlyCache.class).to(AppendOnlyCacheNative.class);
		binder.bind(NextFitHistogram.class).to(VanillaHistogram.class);

		binder.bind(RecordPoolableManager.class);
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
		p.put("maxRecordPages", String.valueOf(maxRecordPages));
		p.put("maxRecordSize", String.valueOf(maxRecordSize));
		p.put("recordPoolLimit", String.valueOf(recordPoolLimit));
		p.put("chunkPoolLimit", String.valueOf(chunkPoolLimit));
		
		return p;
	}

	@Provides
	Pool<Record> recordPoolProvider(final RecordPoolableManager manager,
			@Named("recordPoolLimit") final int limit) {
		return Pools.finitePool(manager, limit);
	}

	
}
