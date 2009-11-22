package com.atteo.jello;

import java.io.File;

import android.content.Context;

import com.atteo.jello.store.StoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class Jello {
	private static Database database;
	private static final int DEVELOPMENT = 1;
	private static int environment;
	private static String fullpath;

	private static final int PRODUCTION = 0;
	private static final int TEST = 2;
	static Injector injector;

	public static String getFullpath() {
		return fullpath;
	}

	public static boolean open(final Context context, final String filename,
			final int environment) {
		Jello.environment = environment;
		loadEnvironment();

		final File file = context.getDatabasePath(filename);
		fullpath = file.getAbsolutePath();

		database = injector.getInstance(Database.class);
		return database.isValid();
	}

	private static void loadEnvironment() {
		switch (environment) {
		case PRODUCTION:
		case DEVELOPMENT:
		case TEST:
			injector = Guice.createInjector(new StoreModule(null));
			break;
		}
	}
}
