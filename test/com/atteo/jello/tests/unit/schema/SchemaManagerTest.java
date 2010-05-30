package com.atteo.jello.tests.unit.schema;

import java.util.HashMap;

import com.atteo.jello.schema.Schema;
import com.atteo.jello.schema.SchemaManager;
import com.atteo.jello.schema.SchemaManagerFactory;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagedFile;
import com.atteo.jello.tests.JelloInterfaceTestCase;
import com.atteo.jello.tests.unit.space.SpaceManagerPolicyMock;
import com.atteo.jello.tests.unit.store.PagedFileMock;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Names;

abstract public class SchemaManagerTest extends
		JelloInterfaceTestCase<SchemaManager> {

	// ---- SETTINGS
	private final short pageSize = 4096;

	private final int klassSchemaManagerPageId = 4;
	// --------------

	private SchemaManager schemaManager;
	@Inject
	private SchemaManagerFactory schemaManagerFactory;
	@Inject
	private PagedFile pagedFile;
	
	@Override
	protected Class<SchemaManager> interfaceUnderTest() {
		return SchemaManager.class;
	}

	@Override
	protected void bindImplementation(Binder binder) {
		binder.bind(SchemaManagerFactory.class).toProvider(
				FactoryProvider.newFactory(SchemaManagerFactory.class,
						implementation()));
	}

	public void configure(Binder binder) {
		binder.requestStaticInjection(Page.class);

		binder.bind(SpaceManagerPolicy.class).to(SpaceManagerPolicyMock.class);
		binder.bind(PagedFile.class).to(PagedFileMock.class);

		final HashMap<String, String> p = new HashMap<String, String>();
		p.put("pageSize", String.valueOf(pageSize));
		Names.bindProperties(binder, p);
	}

	public void testAddSchema() {
		assertNull(schemaManager.getSchema(1));
		Schema schema = new Schema();
		schema.names = new String[1];
		schema.fields = new int[1];
		schema.names[0] = "testField";
		schema.fields[0] = Schema.FIELD_INT;
		schemaManager.addSchema(schema);
		Schema read = schemaManager.getSchema(0);

		assertNotNull(read);

		assertEquals(schema.version, read.version);
		assertEquals(schema.fields.length, read.fields.length);
		for (int i = 0; i < schema.fields.length; i++) {
			assertEquals(schema.fields[i], read.fields[i]);
			assertEquals(schema.names[i], read.names[i]);
		}
	}

	public void testRemoveSchema() {
		assertNull(schemaManager.getSchema(1));
		Schema schema = new Schema();
		schema.version = 1;
		schema.names = new String[1];
		schema.fields = new int[1];
		schema.names[0] = "testField";
		schema.fields[0] = Schema.FIELD_INT;
		schemaManager.addSchema(schema);
		assertNotNull(schemaManager.getSchema(0));
		schemaManager.removeSchema(1);
		assertNotNull(schemaManager.getSchema(0));
		schemaManager.removeSchema(0);
		assertNull(schemaManager.getSchema(0));
	}

	public void testCreate() {
		schemaManager.create();
		schemaManager.load();

		assertNull(schemaManager.getSchema(0));
	}

	public void testLoad() {
		int TESTSIZE = 150;
		schemaManager.create();
		assertNull(schemaManager.getSchema(1));
		for (int i = 0; i < TESTSIZE; i++) {
			Schema schema = new Schema();
			schema.names = new String[2];
			schema.fields = new int[2];
			schema.names[0] = "intField_" + i;
			schema.fields[0] = Schema.FIELD_INT;
			schema.names[1] = "stringField_" + i;
			schema.fields[1] = Schema.FIELD_STRING;
			schemaManager.addSchema(schema);
		}

		schemaManager.commit();

		schemaManager = schemaManagerFactory.create(klassSchemaManagerPageId);

		schemaManager.load();

		for (int i = 0; i < TESTSIZE; i++) {
			Schema schema = schemaManager.getSchema(i);
			assertNotNull(schema);
			assertEquals(2, schema.fields.length);
			assertEquals("intField_" + i, schema.names[0]);
			assertEquals(Schema.FIELD_INT, schema.fields[0]);
			assertEquals("stringField_" + i, schema.names[1]);
			assertEquals(Schema.FIELD_STRING, schema.fields[1]);
		}
		
		assertNull(schemaManager.getSchema(TESTSIZE));
	}

	protected void setUp() {
		super.setUp();

		if (pagedFile.exists())
			pagedFile.remove();
		
		pagedFile.create();
		pagedFile.open();
		pagedFile.addPages(klassSchemaManagerPageId + 1);
		
		schemaManager = schemaManagerFactory.create(klassSchemaManagerPageId);
	}

}
