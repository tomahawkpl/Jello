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
		//dbFile.loadStructure(true);
	}

	@Override
	protected void setUp() throws IOException {
		f = getInstrumentation().getContext().getDatabasePath(filename);
		f.getParentFile().mkdirs();
		if (f.exists())
			f.delete();
		f.createNewFile();
		injector = Guice.createInjector(new StoreModule(f.getAbsolutePath(), null));

		dbFile = injector.getInstance(DatabaseFile.class);

	}

	@Override
	protected void tearDown() {
		dbFile.close();

	}

}