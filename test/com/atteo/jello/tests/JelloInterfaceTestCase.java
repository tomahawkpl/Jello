package com.atteo.jello.tests;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public abstract class JelloInterfaceTestCase<T> extends JelloTestCase {
	protected void bindImplementation(final Binder binder) {
		binder.bind(interfaceUnderTest()).to(implementation());
	}

	@Override
	protected Module extraBindings() {
		final Module s = super.extraBindings();
		Module m = new Module() {

			public void configure(final Binder binder) {
				bindImplementation(binder);
			}

		};

		if (s != null)
			m = Modules.combine(s, m);

		return m;
	}

	protected abstract Class<? extends T> implementation();

	protected abstract Class<T> interfaceUnderTest();

}