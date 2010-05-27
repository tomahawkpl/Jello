package com.atteo.jello.transaction;

import com.atteo.jello.Storable;

public interface TransactionManager {
	public void performInsertTransaction(Storable storable);
	public void performUpdateTransaction(Storable storable);
	public void performDeleteTransaction(Storable storable);
	public boolean performFindTransaction(Storable storable);
}
