package com.atteo.jello;

import java.io.File;

import android.content.Context;

import com.atteo.jello.store.DatabaseFile;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.store.StoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class Jello {
	private static DatabaseFile dbFile;
	private static final int DEVELOPMENT = 1;
	private static int environment;
	private static String fullpath;
	private static PagedFile pagedFile;

	private static final int PRODUCTION = 0;
	private static final int TEST = 2;
	static Injector injector;

	public static String getFullpath() {
		return fullpath;
	}

	public static boolean open(final Context context, final String filename, final boolean readOnly,
			final int environment) {
		final File file = context.getDatabasePath(filename);
		fullpath = file.getAbsolutePath();
		
		boolean isNew = false;
		
		if (!file.exists())
			isNew = true;
		
		if (isNew && readOnly)
			return false;

		Jello.environment = environment;
		loadEnvironment();
		
		pagedFile = injector.getInstance(PagedFile.Factory.class).create(file, readOnly);
		
		dbFile = injector.getInstance(DatabaseFile.Factory.class).create(pagedFile);

		dbFile.loadStructure(isNew);
		
		return dbFile.isValid();
	}

	public static void close() {
		dbFile.close();
	}
	
	private static void loadEnvironment() {
		switch (environment) {
		case PRODUCTION:
		case DEVELOPMENT:
		case TEST:
			injector = Guice.createInjector(new StoreModule(pagedFile, null));
			break;
		}
	}
}
