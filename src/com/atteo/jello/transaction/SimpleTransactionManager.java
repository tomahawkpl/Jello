package com.atteo.jello.transaction;

import android.util.Pool;

import com.atteo.jello.KlassManager;
import com.atteo.jello.Record;
import com.atteo.jello.Storable;
import com.atteo.jello.index.Index;
import com.atteo.jello.schema.Schema;
import com.atteo.jello.schema.SchemaManager;
import com.atteo.jello.schema.StorableWriter;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.google.inject.Inject;

public class SimpleTransactionManager implements TransactionManager {
	private StorableWriter storableWriter;
	private Pool<Record> recordPool;
	private SpaceManagerPolicy spaceManagerPolicy;
	private KlassManager klassManager;
	
	@Inject
	public SimpleTransactionManager(StorableWriter storableWriter, Pool<Record> recordPool,
			SpaceManagerPolicy spaceManagerPolicy, KlassManager klassManager) {
		this.storableWriter = storableWriter;
		this.recordPool = recordPool;
		this.spaceManagerPolicy = spaceManagerPolicy;
		this.klassManager = klassManager;
	}
	
	
	public void performDeleteTransaction(Storable storable) {
		// TODO Auto-generated method stub
		
	}

	public Storable performFindTransaction(Class<? extends Storable> klass,
			int id) {
		// TODO Auto-generated method stub
		return null;
	}

	public void performInsertTransaction(Storable storable) {
		Schema schema = storable.getSchema();
		byte data[] = storableWriter.writeStorable(storable, schema);
		
		Record record = recordPool.acquire();
		spaceManagerPolicy.acquireRecord(record, data.length);
		record.setData(data);
		
		SchemaManager schemaManager = klassManager.getSchemaManagerFor(storable.getStorableClass());
		schemaManager.addSchema(schema);
		
		Index index = klassManager.getIndexFor(storable.getStorableClass());
		index.insert(record);
		
	}

	public void performUpdateTransaction(Storable storable) {
		// TODO Auto-generated method stub
		
	}



}
