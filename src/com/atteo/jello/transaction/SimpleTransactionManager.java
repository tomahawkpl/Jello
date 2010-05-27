package com.atteo.jello.transaction;

import android.util.Pool;

import com.atteo.jello.KlassManager;
import com.atteo.jello.PageUsage;
import com.atteo.jello.Record;
import com.atteo.jello.Storable;
import com.atteo.jello.index.Index;
import com.atteo.jello.schema.Schema;
import com.atteo.jello.schema.SchemaManager;
import com.atteo.jello.schema.StorableWriter;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SimpleTransactionManager implements TransactionManager {
	private StorableWriter storableWriter;
	private SpaceManagerPolicy spaceManagerPolicy;
	private KlassManager klassManager;
	private PagedFile pagedFile;
	private int freeSpaceInfoSize, blockSize;
	private Pool<Page> pagePool;
	private byte[] data;

	@Inject
	public SimpleTransactionManager(StorableWriter storableWriter,
			SpaceManagerPolicy spaceManagerPolicy, KlassManager klassManager,
			PagedFile pagedFile,
			@Named("freeSpaceInfoSize") int freeSpaceInfoSize,
			@Named("blockSize") int blockSize, Pool<Page> pagePool,
			@Named("maxRecordSize") int maxRecordSize) {
		this.storableWriter = storableWriter;
		this.spaceManagerPolicy = spaceManagerPolicy;
		this.klassManager = klassManager;
		this.pagedFile = pagedFile;
		this.freeSpaceInfoSize = freeSpaceInfoSize;
		this.blockSize = blockSize;
		this.pagePool = pagePool;
		
		this.data = new byte[maxRecordSize];
	}

	public void performDeleteTransaction(Storable storable) {
		// TODO Auto-generated method stub

	}

	public boolean performFindTransaction(Storable storable) {
		Class<? extends Storable> klass = storable.getStorableClass();

		if (!klassManager.isKlassManaged(klass))
			return false;

		Record record = storable.getRecord();

		Index index = klassManager.getIndexFor(klass);
		if (!index.find(record))
			return false;

		readRecord(record, data);

		SchemaManager schemaManager = klassManager.getSchemaManagerFor(klass);
		Schema schema = schemaManager.getSchema(record.getSchemaVersion());

		storableWriter.readStorable(data, storable, schema);

		return true;
	}

	public void performInsertTransaction(Storable storable) {
		Class<? extends Storable> klass = storable.getStorableClass();
		Schema schema = storable.getSchema();
		int len = storableWriter.writeStorable(data, storable, schema);

		Record record = storable.getRecord();

		spaceManagerPolicy.acquireRecord(record, len);

		if (!klassManager.isKlassManaged(klass))
			klassManager.addKlass(klass);

		SchemaManager schemaManager = klassManager.getSchemaManagerFor(klass);
		record.setSchemaVersion(schemaManager.addSchema(schema));

		record.setId(klassManager.getIdFor(klass));

		Index index = klassManager.getIndexFor(klass);
		index.insert(record);

		writeRecord(record, data);

	}

	public void performUpdateTransaction(Storable storable) {
		Class<? extends Storable> klass = storable.getStorableClass();
		Schema schema = storable.getSchema();
		int len = storableWriter.writeStorable(data, storable, schema);

		Record record = storable.getRecord();

		spaceManagerPolicy.reacquireRecord(record, len);

		SchemaManager schemaManager = klassManager.getSchemaManagerFor(klass);
		record.setSchemaVersion(schemaManager.addSchema(schema));

		record.setId(klassManager.getIdFor(klass));

		Index index = klassManager.getIndexFor(klass);
		index.insert(record);

		writeRecord(record, data);

	}

	private void readRecord(Record record, byte[] data) {
		int u = record.getPagesUsed();
		int position = 0;

		Page page = pagePool.acquire();

		for (int i = 0; i < u; i++) {
			PageUsage usage = record.getPageUsage(i);
			page.setId(usage.pageId);
			pagedFile.readPage(page);

			byte[] pageData = page.getData();

			for (int j = 0; j < freeSpaceInfoSize; j++) {
				for (int k = 0; k < Byte.SIZE; k++)
					if ((usage.usage[j] & 1 << k) == 1) {
						System.arraycopy(pageData, (j * Byte.SIZE + k)
								* blockSize, data, position, blockSize);
						position += blockSize;
					}
			}
		}

		pagePool.release(page);

	}

	private void writeRecord(Record record, byte data[]) {
		int u = record.getPagesUsed();
		int position = 0;

		Page page = pagePool.acquire();

		for (int i = 0; i < u; i++) {
			PageUsage usage = record.getPageUsage(i);

			page.setId(usage.pageId);
			pagedFile.readPage(page);

			byte[] pageData = page.getData();

			for (int j = 0; j < freeSpaceInfoSize; j++) {
				for (int k = 0; k < Byte.SIZE; k++)
					if ((usage.usage[j] & 1 << k) == 1) {
						System.arraycopy(data, position, pageData, (j
								* Byte.SIZE + k)
								* blockSize, blockSize);

						position += blockSize;
					}
			}
			
			pagedFile.writePage(page);

		}
		
		pagePool.release(page);

	}
}
