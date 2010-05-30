package com.atteo.jello;

import java.util.HashMap;

import android.util.Pool;
import android.util.Pools;

import com.atteo.jello.index.IndexModule;
import com.atteo.jello.klass.KlassManager;
import com.atteo.jello.klass.SimpleKlassManager;
import com.atteo.jello.schema.SchemaModule;
import com.atteo.jello.space.SpaceModule;
import com.atteo.jello.store.PageSizeProvider;
import com.atteo.jello.store.StoreModule;
import com.atteo.jello.transaction.TransactionModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class JelloModule implements Module {
	// ---- SETTINGS
	private final int fileFormatVersion = 0;
	private final String magic = "JelloDatabase";
	
	private final int recordPoolLimit = 8;
	
	private int headerPageId = 0;
	private int freeSpaceInfoPageId = 1;
	private int klassManagerPageId = 2;
	private int minimumPages;
	
	private final int maxRecordPages = 4;
	private final int maxRecordSize;
	// --------------

	private final String fullpath;

	private final HashMap<String, String> properties;

	public JelloModule(	final String fullpath, final HashMap<String, String> properties) {
		this.fullpath = fullpath;
		int pageSize = new PageSizeProvider().get();

		maxRecordSize = maxRecordPages * pageSize;

		this.properties = getDefaultProperties();
		if (properties != null)
			this.properties.putAll(properties);
		
		headerPageId = Integer.valueOf(this.properties.get("headerPageId"));
		freeSpaceInfoPageId = Integer.valueOf(this.properties.get("freeSpaceInfoPageId"));
		klassManagerPageId = Integer.valueOf(this.properties.get("klassManagerPageId"));
		
		minimumPages = Math.max(freeSpaceInfoPageId, klassManagerPageId) + 1;
		
		this.properties.put("minimumPages", String.valueOf(minimumPages));

	}

	public void configure(final Binder binder) {
		binder.install(new StoreModule(fullpath, null));
		binder.install(new SpaceModule(null));
		binder.install(new IndexModule(null));
		binder.install(new SchemaModule(null));
		binder.install(new TransactionModule(null));
		
		binder.bind(KlassManager.class).to(SimpleKlassManager.class);
		
		Names.bindProperties(binder, properties);
		
		binder.requestStaticInjection(Record.class);
		binder.requestStaticInjection(PageUsage.class);
		binder.requestStaticInjection(Storable.class);
	}
	
	private HashMap<String, String> getDefaultProperties() {
		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("fileFormatVersion", String.valueOf(fileFormatVersion));
		p.put("magic", String.valueOf(magic));
		p.put("recordPoolLimit", String.valueOf(recordPoolLimit));
		p.put("headerPageId", String.valueOf(headerPageId));
		p.put("freeSpaceInfoPageId", String.valueOf(freeSpaceInfoPageId));
		p.put("klassManagerPageId", String.valueOf(klassManagerPageId));
		p.put("minimumPages", String.valueOf(minimumPages));
		p.put("maxRecordPages", String.valueOf(maxRecordPages));
		p.put("maxRecordSize", String.valueOf(maxRecordSize));
		return p;
	}

	@Provides @Singleton
	Pool<Record> recordPoolProvider(final RecordPoolableManager manager,
			@Named("recordPoolLimit") final int limit) {
		return Pools.finitePool(manager, limit);
	}


}
