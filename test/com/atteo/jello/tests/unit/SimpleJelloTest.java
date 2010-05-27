package com.atteo.jello.tests.unit;

import java.io.File;

import com.atteo.jello.DatabaseField;
import com.atteo.jello.Jello;
import com.atteo.jello.Storable;
import com.atteo.jello.tests.JelloTestCase;
import com.google.inject.Binder;

public class SimpleJelloTest extends JelloTestCase {

	public void configure(Binder binder) {

	}

	public void testInsert() {
		assertEquals(Jello.OPEN_SUCCESS, Jello.open(this.getInstrumentation()
				.getContext(), "testfile"));

		TestObject object = new TestObject();
		object.name = "person";
		object.age = 25;
		object.save();

		TestObject read = new TestObject();
		read.setId(object.getId());

		assertTrue(read.load());
		assertEquals(object.name, read.name);
		assertEquals(object.age, read.age);
	}

	private class TestObject extends Storable {
		@DatabaseField
		String name;
		@DatabaseField
		int age;
	}

	public void setUp() {
		final File file = this.getInstrumentation().getContext()
				.getDatabasePath("testfile");
		file.delete();
	}

	public void tearDown() {

	}

}
