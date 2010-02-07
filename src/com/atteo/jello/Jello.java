package com.atteo.jello;

import java.io.File;
import java.io.IOException;

import android.content.Context;

import com.atteo.jello.store.DatabaseFile;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.store.StoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class Jello {
	public static final int OPEN_FAILED = 0;
	public static final int OPEN_READONLY = 1;
	public static final int OPEN_SUCCESS = 2;
	
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

	public static int open(final Context context, final String filename,
			final int environment) {
		final File file = context.getDatabasePath(filename);
		fullpath = file.getAbsolutePath();

		boolean isNew = false;

		if (!file.exists())
			isNew = true;

		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return Jello.OPEN_FAILED;
		}

		Jello.environment = environment;
		loadEnvironment();

		dbFile = injector.getInstance(DatabaseFile.class);

		dbFile.loadStructure(isNew);

		if (!dbFile.isValid())
			return Jello.OPEN_FAILED;
		else
			return Jello.OPEN_SUCCESS;
	}

	public static void close() {
		dbFile.close();
	}

	private static void loadEnvironment() {
		switch (environment) {
		case PRODUCTION:
		case DEVELOPMENT:
		case TEST:
			injector = Guice.createInjector(new StoreModule(fullpath, null));
			break;
		}
	}
}
