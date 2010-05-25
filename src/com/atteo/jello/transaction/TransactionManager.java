package com.atteo.jello.transaction;

import com.atteo.jello.Storable;

public interface TransactionManager {
	public void performInsertTransaction(Storable storable);
	public void performUpdateTransaction(Storable storable);
	public void performDeleteTransaction(Storable storable);
	public Storable performFindTransaction(Class<? extends Storable> klass, int id);
}
