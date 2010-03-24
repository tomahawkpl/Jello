package com.atteo.jello.tests;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.easymock.EasyMock;

import android.test.InstrumentationTestCase;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;

abstract public class JelloTestCase extends InstrumentationTestCase implements
		Module {

	public JelloTestCase() {
		prepareInjector(null);
	}

	public JelloTestCase(Module s) {
		super();
		prepareInjector(s);
	}

	protected Module extraBindings() {
		return null;
	}

	private void prepareInjector(Module s) {
		final HashMap<Class<?>, Object> mocks = new HashMap<Class<?>, Object>();

		Class<?> klass = this.getClass();

		while (!klass.equals(JelloTestCase.class)) {
			Field fields[] = klass.getDeclaredFields();

			for (Field field : fields) {
				if (field.isAnnotationPresent(Mock.class)) {
					Object m = EasyMock.createMock(field.getType());
					mocks.put(field.getType(), m);
					field.setAccessible(true);
					try {
						field.set(this, m);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			
			klass = klass.getSuperclass();
		}
		Module m = new Module() {

			@SuppressWarnings("unchecked")
			public void configure(Binder binder) {
				Set<Class<?>> keys = mocks.keySet();
				Iterator<Class<?>> i = keys.iterator();
				while (i.hasNext()) {
					Class<?> klass = i.next();
					TypeLiteral t = TypeLiteral.get(klass);
					Object o = mocks.get(klass);
					binder.bind(t).toInstance(o);
				}
			}

		};

		if (extraBindings() != null)
			m = Modules.combine(extraBindings(), m);

		Injector injector = Guice.createInjector(m, new CommonBindings(), this);
		injector.injectMembers(this);

	}

}
