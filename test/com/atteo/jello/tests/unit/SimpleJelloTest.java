package com.atteo.jello.tests.unit;

import java.io.File;

import com.atteo.jello.Jello;
import com.atteo.jello.Storable;
import com.atteo.jello.associations.DatabaseField;
import com.atteo.jello.index.BTree;
import com.atteo.jello.index.Index;
import com.atteo.jello.klass.KlassManager;
import com.atteo.jello.tests.JelloTestCase;
import com.google.inject.Binder;

public class SimpleJelloTest extends JelloTestCase {
		
	public void configure(final Binder binder) {

	}

	@Override
	public void setUp() {
		final File file = getInstrumentation().getContext().getDatabasePath(
				"testfile");
		file.delete();
	}

	@Override
	public void tearDown() {
		Jello.close();
	}

	public void testInsert() {
		final int TESTSIZE = 500;

		assertEquals(Jello.OPEN_SUCCESS, Jello.open(getInstrumentation()
				.getContext(), "testfile"));

		for (int i = 0; i < TESTSIZE; i++) {
			final TestObject object = new TestObject();
			object.name = "person";
			object.age = i;
			object.save();
		}
		
		KlassManager klassManager = Jello.getInjector().getInstance(KlassManager.class);
		Index index = klassManager.getIndexFor(TestObject.class.getCanonicalName());
		((BTree)index).debug();
		
		TestObject read = new TestObject();
		for (int i = 0; i < TESTSIZE; i++) {
			read.setId(i);
			assertNotNull(read.load());
			assertEquals("person", read.name);
			assertEquals(i, read.age);
		}
		
		for (int i = 0; i < TESTSIZE; i++) {
			final TestObject2 object = new TestObject2();
			object.name = "car" + i;
			object.speed = i;
			object.type = i * 3;
			object.save();
		}

		index = klassManager.getIndexFor(TestObject2.class.getCanonicalName());
		((BTree)index).debug();
		
		TestObject2 read2 = new TestObject2();
		for (int i = 0; i < TESTSIZE; i++) {
			read2.setId(i);
			assertNotNull(read2.load());
			assertEquals("car" + i, read2.name);
			assertEquals(i, read2.speed);
			assertEquals(i * 3, read2.type);
		}

		Jello.close();
		
		assertEquals(Jello.OPEN_SUCCESS, Jello.open(getInstrumentation()
				.getContext(), "testfile"));
		
		read = new TestObject();
		for (int i = 0; i < TESTSIZE; i++) {
			read.setId(i);
			assertNotNull(read.load());
			assertEquals("person", read.name);
			assertEquals(i, read.age);
		}
		
		read2 = new TestObject2();
		for (int i = 0; i < TESTSIZE; i++) {
			read2.setId(i);
			assertNotNull(read2.load());
			assertEquals("car" + i, read2.name);
			assertEquals(i, read2.speed);
			assertEquals(i * 3, read2.type);
		}
		
	}
	
	public void testDelete() {
		final int TESTSIZE = 10;

		assertEquals(Jello.OPEN_SUCCESS, Jello.open(getInstrumentation()
				.getContext(), "testfile"));

		for (int i = 0; i < TESTSIZE; i++) {
			final TestObject object = new TestObject();
			object.name = "person";
			object.age = i;
			object.save();
		}
		
		for (int i = 0; i < TESTSIZE; i++) {
			final TestObject2 object = new TestObject2();
			object.name = "car";
			object.speed = i;
			object.type = i * 3;
			object.save();
		}
	
		TestObject read = new TestObject();
		for (int i = 0; i < TESTSIZE; i++) {
			read.setId(i);
			read.remove();
		}
		
		read = new TestObject();
		for (int i = 0; i < TESTSIZE; i++) {
			read.setId(i);
			assertNull(read.load());
		}
		
		Jello.close();
		
		assertEquals(Jello.OPEN_SUCCESS, Jello.open(getInstrumentation()
				.getContext(), "testfile"));
		
		read = new TestObject();
		for (int i = 0; i < TESTSIZE; i++) {
			read.setId(i);
			assertNull(read.load());
		}

		TestObject2 read2 = new TestObject2();
		for (int i = 0; i < TESTSIZE; i++) {
			read2.setId(i);
			assertNotNull(read2.load());
			assertEquals("car", read2.name);
			assertEquals(i, read2.speed);
			assertEquals(i * 3, read2.type);
		}
	}

	private class TestObject extends Storable {
		@DatabaseField
		String name;
		@DatabaseField
		int age;
	}
	
	private class TestObject2 extends Storable {
		@DatabaseField
		String name;
		@DatabaseField
		int type;
		@DatabaseField
		int speed;
	}

}
