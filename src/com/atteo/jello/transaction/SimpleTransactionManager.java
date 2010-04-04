package com.atteo.jello.transaction;

import com.atteo.jello.Record;
import com.atteo.jello.Storable;
import com.atteo.jello.StorableCollection;

public class SimpleTransactionManager implements TransactionManager {

	public void performDeleteTransaction(Class<? extends Storable> klass,
			Record record) {
		// TODO Auto-generated method stub

	}

	public Storable performFindTransaction(Class<? extends Storable> klass,
			Record record) {
		// TODO Auto-generated method stub
		return null;
	}

	public void performInsertTransaction(Class<? extends Storable> klass,
			Record record) {
		// TODO Auto-generated method stub

	}

	public <T extends Storable> StorableCollection<T> performSearchTransaction(
			Class<? extends Storable> klass, Record record) {
		// TODO Auto-generated method stub
		return null;
	}

	public void performUpdateTransaction(Class<? extends Storable> klass,
			Record record) {
		// TODO Auto-generated method stub

	}

}
