package com.atteo.jello;

import android.util.Log;

public class StorableFactory<T extends Storable> {
	static public <T extends Storable> T createStorable(final Class<T> klass) {
		try {
			return klass.newInstance();
		} catch (final IllegalAccessException e) {
			Log.e("Jello", "Class instantiation failed", e);
			throw new RuntimeException(
					"Class '"
							+ klass.getSimpleName()
							+ "' couldn't be instantiated (is the default constructor public?)");
		} catch (final InstantiationException e) {
			Log.e("Jello", "Class instantiation failed", e);
			throw new RuntimeException(
					"Class '"
							+ klass.getSimpleName()
							+ " couldn't be instantiated (is the default constructor public?)");
		}

	}

}
