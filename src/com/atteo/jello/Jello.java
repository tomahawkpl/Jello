package com.atteo.jello;

import java.io.File;
import java.io.IOException;

import android.content.Context;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Jello {
	public static final int OPEN_FAILED = 0;
	public static final int OPEN_READONLY = 1;
	public static final int OPEN_SUCCESS = 2;

	private static DatabaseFile dbFile;
	private static String fullpath;

	private static boolean isOpened = false;
	
	static Injector injector;

	public static void close() {
		dbFile.close();
		isOpened = false;
	}

	public static String getFullpath() {
		return fullpath;
	}

	public static int open(final Context context, final String filename) {
		final File file = context.getDatabasePath(filename);
		fullpath = file.getAbsolutePath();

		boolean isNew = false;

		if (!file.exists())
			isNew = true;

		if (isNew)
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (final IOException e) {
				e.printStackTrace();
				return Jello.OPEN_FAILED;
			}

		loadEnvironment();

		dbFile = injector.getInstance(DatabaseFile.class);

		dbFile.open();

		if (isNew)
			if (dbFile.createStructure())
				return Jello.OPEN_SUCCESS;
			else
				return Jello.OPEN_FAILED;

		if (!dbFile.loadHeader())
			return Jello.OPEN_FAILED;

		injector = Guice.createInjector(new JelloModule(fullpath, dbFile.getReadProperties()));

		// recreate dbFile in case pageSize or other basic settings
		// read from the file have values different that the defaults
		dbFile = injector.getInstance(DatabaseFile.class);
		if (!dbFile.loadHeader())
			return Jello.OPEN_FAILED;

		if (!dbFile.loadStructure())
			return Jello.OPEN_FAILED;

		isOpened = true;
		return Jello.OPEN_SUCCESS;
	}
	
	public static boolean isOpened() {
		return isOpened;
	}

	public static void setInjector(final Injector injector) {
		Jello.injector = injector;
	}

	public static Injector getInjector() {
		return injector;
	}
	
	private static Injector createGuiceInjector() {
		return Guice.createInjector(new JelloModule(fullpath, null));
	}

	private static void loadEnvironment() {
		injector = createGuiceInjector();
	}
}