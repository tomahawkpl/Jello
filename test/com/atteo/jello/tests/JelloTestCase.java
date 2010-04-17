package com.atteo.jello.tests;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.easymock.EasyMock;

import android.os.Debug;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.atteo.jello.Jello;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;

abstract public class JelloTestCase extends InstrumentationTestCase implements
		Module {

	protected Module s = null;
	protected long testStart;
	protected boolean dumpingTrace = false;

	public JelloTestCase() {
		super();
	}

	public JelloTestCase(final Module s) {
		super();
		this.s = s;

	}

	// prepareInjector doesn't show thrown exceptions if is placed somewhere
	// else
	// that's why super.setUp() call is needed by each test class
	protected void setUp() {
		prepareInjector(s);
	}

	protected void prepareInjector(final Module s) {
		final HashMap<Class<?>, Object> mocks = new HashMap<Class<?>, Object>();

		Class<?> klass = this.getClass();

		while (!klass.equals(JelloTestCase.class)) {
			final Field fields[] = klass.getDeclaredFields();

			for (final Field field : fields)
				if (field.isAnnotationPresent(Mock.class)) {
					final Object m = EasyMock.createMock(field.getType());
					mocks.put(field.getType(), m);
					field.setAccessible(true);
					try {
						field.set(this, m);
					} catch (final IllegalArgumentException e) {
						e.printStackTrace();
					} catch (final IllegalAccessException e) {
						e.printStackTrace();
					}
				}

			klass = klass.getSuperclass();
		}

		Module m = new Module() {

			@SuppressWarnings("unchecked")
			public void configure(final Binder binder) {
				final Set<Class<?>> keys = mocks.keySet();
				final Iterator<Class<?>> i = keys.iterator();
				while (i.hasNext()) {
					final Class<?> klass = i.next();
					final TypeLiteral t = TypeLiteral.get(klass);
					final Object o = mocks.get(klass);
					binder.bind(t).toInstance(o);
				}
			}

		};

		if (extraBindings() != null)
			m = Modules.combine(extraBindings(), m);

		Injector injector = Guice.createInjector(new CommonBindings(), m, this);

		injector.injectMembers(this);

		Jello.setInjector(injector);

	}

	protected Module extraBindings() {
		return null;
	}

	protected void startPerformanceTest(boolean dumpTrace) {
		dumpingTrace = dumpTrace;
		if (dumpTrace)
			Debug.startMethodTracing("jello/" + this.getClass().getSimpleName() + "." + getName());
		testStart = System.currentTimeMillis();

	}

	protected void endPerformanceTest() {
		long testEnd = System.currentTimeMillis();
		if (dumpingTrace)
			Debug.stopMethodTracing();
		Log.i("jello", "Test '" + this.getClass().getSimpleName() + "." + getName() + "' took " + (testEnd - testStart)
				+ "ms");
	}
}