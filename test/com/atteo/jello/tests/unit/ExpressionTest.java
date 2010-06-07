package com.atteo.jello.tests.unit;

import com.atteo.jello.Expression;
import com.atteo.jello.PageUsage;
import com.atteo.jello.Record;
import com.atteo.jello.Storable;
import com.atteo.jello.schema.StorableWriter;
import com.atteo.jello.tests.JelloTestCase;
import com.atteo.jello.tests.unit.schema.StorableWriterMock;
import com.atteo.jello.tests.unit.transaction.TransactionManagerMock;
import com.atteo.jello.transaction.TransactionManager;
import com.google.inject.Binder;
import com.google.inject.name.Names;

public class ExpressionTest extends JelloTestCase {

	public void configure(Binder binder) {
		binder.requestStaticInjection(Record.class);
		binder.requestStaticInjection(PageUsage.class);
		binder.requestStaticInjection(Storable.class);
		binder.bind(StorableWriter.class).to(StorableWriterMock.class);
		binder.bind(TransactionManager.class).to(TransactionManagerMock.class);

		binder.bind(Integer.class).annotatedWith(Names.named("maxRecordPages"))
				.toInstance(4);
		binder.bind(Short.class)
				.annotatedWith(Names.named("freeSpaceInfoSize")).toInstance(
						(short) 4);
		binder.bind(Short.class).annotatedWith(Names.named("pageSize"))
				.toInstance((short) 4096);
	}

	@Override
	public void setUp() {
		super.setUp();
	}

	public void testInt() {
		TestClass t = new TestClass();

		t.fieldInt = 5;

		assertTrue(new Expression(".fieldInt", Expression.OPERATOR_EQUAL, 5)
				.evaluate(t));
		assertFalse(new Expression(".fieldInt", Expression.OPERATOR_EQUAL, 6)
				.evaluate(t));
		assertTrue(new Expression(".fieldInt", Expression.OPERATOR_NOT_EQUAL, 4)
				.evaluate(t));
		assertTrue(new Expression(".fieldInt", Expression.OPERATOR_GREATER, 4)
				.evaluate(t));
		assertTrue(new Expression(".fieldInt",
				Expression.OPERATOR_GREATER_EQUAL, 5).evaluate(t));
		assertFalse(new Expression(".fieldInt", Expression.OPERATOR_LOWER, 4)
				.evaluate(t));
		assertTrue(new Expression(".fieldInt", Expression.OPERATOR_LOWER_EQUAL,
				5).evaluate(t));
	}

	public void testLogic() {
		TestClass t = new TestClass();

		t.fieldInt = 5;
		t.fieldString = "value";

		assertTrue(new Expression(new Expression(".fieldInt",
				Expression.OPERATOR_EQUAL, 5), Expression.OPERATOR_AND,
				new Expression(".fieldString", Expression.OPERATOR_EQUAL,
						"value")).evaluate(t));
		assertFalse(new Expression(new Expression(".fieldInt",
				Expression.OPERATOR_EQUAL, 5), Expression.OPERATOR_AND,
				new Expression(".fieldString", Expression.OPERATOR_EQUAL,
						"value2")).evaluate(t));
		assertTrue(new Expression(new Expression(".fieldInt",
				Expression.OPERATOR_EQUAL, 5), Expression.OPERATOR_OR,
				new Expression(".fieldString", Expression.OPERATOR_EQUAL,
						"value2")).evaluate(t));
		assertFalse(new Expression(new Expression(".fieldInt",
				Expression.OPERATOR_NOT_EQUAL, 5), Expression.OPERATOR_OR,
				new Expression(".fieldString", Expression.OPERATOR_EQUAL,
						"value2")).evaluate(t));

	}

}
