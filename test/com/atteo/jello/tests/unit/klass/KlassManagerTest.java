package com.atteo.jello.tests.unit.klass;

import java.util.HashMap;

import com.atteo.jello.PageUsage;
import com.atteo.jello.Record;
import com.atteo.jello.Storable;
import com.atteo.jello.index.IndexFactory;
import com.atteo.jello.klass.KlassManager;
import com.atteo.jello.schema.SchemaManagerFactory;
import com.atteo.jello.space.Hybrid;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.atteo.jello.tests.unit.index.IndexMock;
import com.atteo.jello.tests.unit.schema.SchemaManagerMock;
import com.atteo.jello.tests.unit.store.PagedFileMock;
import com.atteo.jello.tests.unit.transaction.TransactionManagerMock;
import com.atteo.jello.transaction.TransactionManager;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Names;

public abstract class KlassManagerTest extends
		JelloInterfaceTestCase<KlassManager> {

	// ---- SETTINGS
	private final short pageSize = 4096;
	private final short blockSize = 128;
	private final short freeSpaceInfosPerPage = 1023;
	private final short freeSpaceInfoPageCapacity = 4092;
	private final short freeSpaceInfoSize = 4;
	private final int freeSpaceInfoPageId = 1;
	private final int klassManagerPageId = 2;
	private final int maxRecordPages = 4;
	private final int maxRecordSize = maxRecordPages * pageSize;
	private final int nextFitHistogramClasses = 8;

	@Inject
	private PagedFile pagedFile;
	@Inject
	private SpaceManagerPolicy policy;
	@Inject
	private KlassManager klassManager;

	@Override
	protected Class<KlassManager> interfaceUnderTest() {
		return KlassManager.class;
	}

	public void configure(Binder binder) {
		binder.requestStaticInjection(Record.class);
		binder.requestStaticInjection(Page.class);
		binder.requestStaticInjection(PageUsage.class);
		binder.requestStaticInjection(Storable.class);

		binder.bind(PagedFile.class).to(PagedFileMock.class);
		binder.bind(SpaceManagerPolicy.class).to(Hybrid.class);
		binder.bind(TransactionManager.class).to(TransactionManagerMock.class);

		binder.bind(IndexFactory.class)
				.toProvider(
						FactoryProvider.newFactory(IndexFactory.class,
								IndexMock.class));

		binder.bind(SchemaManagerFactory.class).toProvider(
				FactoryProvider.newFactory(SchemaManagerFactory.class,
						SchemaManagerMock.class));

		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("blockSize", String.valueOf(blockSize));
		p.put("pageSize", String.valueOf(pageSize));
		p.put("freeSpaceInfoSize", String.valueOf(freeSpaceInfoSize));
		p.put("freeSpaceInfoPageCapacity", String
				.valueOf(freeSpaceInfoPageCapacity));
		p.put("freeSpaceInfosPerPage", String.valueOf(freeSpaceInfosPerPage));
		p.put("freeSpaceInfoPageId", String.valueOf(freeSpaceInfoPageId));
		p.put("klassManagerPageId", String.valueOf(klassManagerPageId));
		p.put("maxRecordSize", String.valueOf(maxRecordSize));
		p.put("maxRecordPages", String.valueOf(maxRecordPages));
		p.put("nextFitHistogramClasses", String
				.valueOf(nextFitHistogramClasses));

		Names.bindProperties(binder, p);
	}

	public void testAddKlass() {
		String className = new TestClass().getClassName();
		klassManager.addKlass(className);
		assertTrue(klassManager.isKlassManaged(className));
	}

	public void testCreate() {
		klassManager.create();
		klassManager.load();

		String className = new TestClass().getClassName();
		klassManager.addKlass(className);
		assertTrue(klassManager.isKlassManaged(className));
	}

	public void testLoad() {
		int TESTSIZE = 250;

		klassManager.create();

		for (int i = 0; i < TESTSIZE; i++) {
			String name = String.valueOf(i);
			assertFalse(klassManager.isKlassManaged(name));
			klassManager.addKlass(name);
			assertTrue(klassManager.isKlassManaged(name));
		}
		
		klassManager.commit();
		klassManager.load();
		
		for (int i=0;i<TESTSIZE;i++) {
			String name = String.valueOf(i);
			assertTrue(klassManager.isKlassManaged(name));
		}
	}

	@Override
	protected void setUp() {
		super.setUp();
		if (!pagedFile.exists())
			pagedFile.create();
		pagedFile.open();
		pagedFile.addPages(5);

		policy.create();
		klassManager.create();
	}

	@Override
	protected void tearDown() {
		pagedFile.close();
		pagedFile.remove();
	}

	private class TestClass extends Storable {

	}

}
