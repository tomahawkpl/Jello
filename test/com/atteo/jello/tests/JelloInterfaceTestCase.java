package com.atteo.jello.tests;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.util.Modules;


public abstract class JelloInterfaceTestCase<T> extends JelloTestCase {
	@Override
	protected Module extraBindings() {
		Module s = super.extraBindings();
		Module m = new Module() {

			public void configure(Binder binder) {
				binder.bind(classUnderTest()).to(implementation());
			}
			
		};
		
		if (s != null)
			m = Modules.combine(s,m);
		
		return m;
	}
	
	protected abstract Class<T> classUnderTest();
	protected abstract Class<? extends T> implementation();
}