package com.atteo.jello.tests.unit;

import java.io.File;
import java.util.ArrayList;

import com.atteo.jello.Expression;
import com.atteo.jello.Jello;
import com.atteo.jello.StorableCollection;
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
			final TestClass object = new TestClass();
			object.fieldString = "person";
			object.fieldInt = i;
			object.save();
		}

		KlassManager klassManager = Jello.getInjector().getInstance(
				KlassManager.class);
		Index index = klassManager.getIndexFor(TestClass.class
				.getCanonicalName());
		((BTree) index).debug();

		TestClass read = new TestClass();
		for (int i = 0; i < TESTSIZE; i++) {
			read.setId(i);
			assertNotNull(read.load());
			assertEquals("person", read.fieldString);
			assertEquals(i, read.fieldInt);
		}

		for (int i = 0; i < TESTSIZE; i++) {
			final TestClassParent object = new TestClassParent();
			object.fieldString = "car" + i;
			object.fieldInt = i;
			object.fieldShort = (short)(i * 3);
			object.save();
		}

		index = klassManager.getIndexFor(TestClassParent.class.getCanonicalName());
		((BTree) index).debug();

		TestClassParent read2 = new TestClassParent();
		for (int i = 0; i < TESTSIZE; i++) {
			read2.setId(i);
			assertNotNull(read2.load());
			assertEquals("car" + i, read2.fieldString);
			assertEquals(i, read2.fieldInt);
			assertEquals(i * 3, read2.fieldShort);
		}

		Jello.close();

		assertEquals(Jello.OPEN_SUCCESS, Jello.open(getInstrumentation()
				.getContext(), "testfile"));

		read = new TestClass();
		for (int i = 0; i < TESTSIZE; i++) {
			read.setId(i);
			assertNotNull(read.load());
			assertEquals("person", read.fieldString);
			assertEquals(i, read.fieldInt);
		}

		read2 = new TestClassParent();
		for (int i = 0; i < TESTSIZE; i++) {
			read2.setId(i);
			assertNotNull(read2.load());
			assertEquals("car" + i, read2.fieldString);
			assertEquals(i, read2.fieldInt);
			assertEquals(i * 3, read2.fieldShort);
		}

	}

	public void testDelete() {
		final int TESTSIZE = 10;

		assertEquals(Jello.OPEN_SUCCESS, Jello.open(getInstrumentation()
				.getContext(), "testfile"));

		for (int i = 0; i < TESTSIZE; i++) {
			final TestClass object = new TestClass();
			object.fieldString = "person";
			object.fieldInt = i;
			object.save();
		}

		for (int i = 0; i < TESTSIZE; i++) {
			final TestClassParent object = new TestClassParent();
			object.fieldString = "car";
			object.fieldInt = i;
			object.fieldShort = (short) (i * 3);
			object.save();
		}

		TestClass read = new TestClass();
		for (int i = 0; i < TESTSIZE; i++) {
			read.setId(i);
			read.remove();
		}

		read = new TestClass();
		for (int i = 0; i < TESTSIZE; i++) {
			read.setId(i);
			assertNull(read.load());
		}

		Jello.close();

		assertEquals(Jello.OPEN_SUCCESS, Jello.open(getInstrumentation()
				.getContext(), "testfile"));

		read = new TestClass();
		for (int i = 0; i < TESTSIZE; i++) {
			read.setId(i);
			assertNull(read.load());
		}

		TestClassParent read2 = new TestClassParent();
		for (int i = 0; i < TESTSIZE; i++) {
			read2.setId(i);
			assertNotNull(read2.load());
			assertEquals("car", read2.fieldString);
			assertEquals(i, read.fieldInt);
			assertEquals(i * 3, read2.fieldShort);
		}
	}

	public void testStorableCollection() {
		final int TESTSIZE = 100;

		assertEquals(Jello.OPEN_SUCCESS, Jello.open(getInstrumentation()
				.getContext(), "testfile"));

		for (int i = 0; i < TESTSIZE; i++) {
			final TestClass object = new TestClass();
			object.fieldString = "person";
			object.fieldInt = i;
			object.save();
		}

		StorableCollection<TestClass> collection = new StorableCollection<TestClass>(
				TestClass.class);

		assertEquals(TESTSIZE, collection.getCount());

		collection.where(new Expression(".fieldInt",
				Expression.OPERATOR_GREATER_EQUAL, TESTSIZE / 2));

		ArrayList<TestClass> array = collection.toArrayList();

		int l = array.size();
		assertEquals(TESTSIZE / 2, l);
		for (int i = 0; i < l; i++)
			assertTrue(array.get(i).fieldInt >= TESTSIZE / 2);

	}


}
