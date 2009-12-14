package com.atteo.jello.tests.unit.store;

import java.io.File;
import java.io.IOException;

import android.test.InstrumentationTestCase;

import com.atteo.jello.store.DatabaseFile;
import com.atteo.jello.store.StoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class DatabaseFileTest extends InstrumentationTestCase {
	private static final String filename = "testfile";
	private Injector injector;
	private DatabaseFile dbFile;
	private File f;

	public void testCreateStructure() {
		dbFile.loadStructure(true);
	}

	@Override
	protected void setUp() throws IOException {
		injector = Guice.createInjector(new StoreModule(null));
		f = getInstrumentation().getContext().getDatabasePath(filename);
		f.getParentFile().mkdirs();
		if (f.exists())
			f.delete();
		f.createNewFile();
		DatabaseFile.Factory dbFactory = injector
				.getInstance(DatabaseFile.Factory.class);
		dbFile = dbFactory.create(f, false);

	}

	@Override
	protected void tearDown() {
		dbFile.close();

	}

}