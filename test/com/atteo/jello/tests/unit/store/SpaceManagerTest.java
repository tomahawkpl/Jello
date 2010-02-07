package com.atteo.jello.tests.unit.store;

import java.io.File;
import java.io.IOException;

import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.store.DatabaseFile;
import com.atteo.jello.store.StoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import android.test.InstrumentationTestCase;

public class SpaceManagerTest extends InstrumentationTestCase {
	private static final String filename = "testfile";
	private Injector injector;
	private DatabaseFile dbFile;
	private File f;
	
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
	
	public void testAcquireReleasePage() {

	}
	
	@Override
	protected void tearDown() {
		dbFile.close();
	}
}
