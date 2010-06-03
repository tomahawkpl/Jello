package com.atteo.jello.space;

import java.util.HashMap;

import com.atteo.jello.store.PageSizeProvider;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class SpaceModule implements Module {
	// ---- SETTINGS

	private final int histogramClasses = 8;

	private final short blockSize = 64;
	private final short blocksPerPage;
	private final short freeSpaceInfoSize;
	private final short freeSpaceInfosPerPage;
	private final short freeSpaceInfoPageCapacity;

	// --------------

	private final short pageSize;

	private final HashMap<String, String> properties;

	public SpaceModule(final HashMap<String, String> properties) {
		pageSize = new PageSizeProvider().get();

		blocksPerPage = (short) (pageSize / blockSize);
		freeSpaceInfoSize = (short) (blocksPerPage / (short) Byte.SIZE);
		freeSpaceInfoPageCapacity = (short) (pageSize - 4);
		freeSpaceInfosPerPage = (short) (freeSpaceInfoPageCapacity / freeSpaceInfoSize);

		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);

	}

	public void configure(final Binder binder) {
		Names.bindProperties(binder, properties);

		binder.bind(SpaceManagerPolicy.class).to(Hybrid.class);
	}

	private HashMap<String, String> getDefaultProperties() {
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("nextFitHistogramClasses", String.valueOf(histogramClasses));
		p.put("blockSize", String.valueOf(blockSize));
		p.put("blockPerPage", String.valueOf(blocksPerPage));
		p.put("freeSpaceInfoSize", String.valueOf(freeSpaceInfoSize));
		p.put("freeSpaceInfoPageCapacity", String
				.valueOf(freeSpaceInfoPageCapacity));
		p.put("freeSpaceInfosPerPage", String.valueOf(freeSpaceInfosPerPage));

		return p;
	}
}
