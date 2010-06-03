package com.atteo.jello.transaction;

import com.atteo.jello.Storable;

public interface TransactionManager {
	public void performDeleteTransaction(Storable storable);

	public boolean performFindTransaction(Storable storable);

	public void performInsertTransaction(Storable storable);

	public void performUpdateTransaction(Storable storable);
}
