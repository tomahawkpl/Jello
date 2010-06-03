package com.atteo.jello.tests.unit;

import java.io.File;

import com.atteo.jello.DatabaseField;
import com.atteo.jello.Jello;
import com.atteo.jello.Storable;
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

	}

	public void testInsert() {
		final int TESTSIZE = 100;

		assertEquals(Jello.OPEN_SUCCESS, Jello.open(getInstrumentation()
				.getContext(), "testfile"));

		for (int i = 0; i < TESTSIZE; i++) {
			final TestObject object = new TestObject();
			object.name = "person";
			object.age = i;
			object.save();
		}

		final TestObject read = new TestObject();
		for (int i = 0; i < TESTSIZE; i++) {
			read.setId(i);
			assertTrue(read.load());
			assertEquals("person", read.name);
			assertEquals(i, read.age);
		}

		Jello.close();
	}

	private class TestObject extends Storable {
		@DatabaseField
		String name;
		@DatabaseField
		int age;
	}

}
