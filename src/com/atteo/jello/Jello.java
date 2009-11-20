package com.atteo.jello;

import java.io.File;

import com.google.inject.Guice;
import com.google.inject.Injector;

import android.content.Context;

public class Jello {
	private static Database database;
	private static String fullpath;
	static Injector injector;
	private static int environment;
	
	private static final int PRODUCTION = 0;
	private static final int DEVELOPMENT = 1;
	private static final int TEST = 2;
	
	public static boolean open(Context context, String filename, int environment) {
		Jello.environment = environment;
		loadEnvironment();

		File file = context.getDatabasePath(filename);
		fullpath = file.getAbsolutePath();
		
		database = injector.getInstance(Database.class);
		return database.isValid();
	}

	private static void loadEnvironment() {
		switch(environment) {
		case PRODUCTION:
		case DEVELOPMENT:
		case TEST:
			injector = Guice.createInjector(new StoreModule(null));
			break;
		}
	}

	public static String getFullpath() {
		return fullpath;
	}
}
