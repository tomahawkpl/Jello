package com.atteo.jello.transaction;

import com.atteo.jello.Record;
import com.atteo.jello.Storable;
import com.atteo.jello.StorableCollection;

public interface TransactionManager {
	public void performInsertTransaction(Class<? extends Storable> klass, Record record);
	public void performUpdateTransaction(Class<? extends Storable> klass, Record record);
	public void performDeleteTransaction(Class<? extends Storable> klass, Record record);
	public Storable performFindTransaction(Class<? extends Storable> klass, Record record);
	public <T extends Storable> StorableCollection<T> performSearchTransaction(Class<? extends Storable> klass, Record record);
}
