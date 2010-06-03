package com.atteo.jello.transaction;

import android.util.Pool;

import com.atteo.jello.PageUsage;
import com.atteo.jello.Record;
import com.atteo.jello.Storable;
import com.atteo.jello.index.Index;
import com.atteo.jello.klass.KlassManager;
import com.atteo.jello.schema.Schema;
import com.atteo.jello.schema.SchemaManager;
import com.atteo.jello.schema.StorableWriter;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.store.Page;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SimpleTransactionManager implements TransactionManager {
	private final StorableWriter storableWriter;
	private final SpaceManagerPolicy spaceManagerPolicy;
	private final KlassManager klassManager;
	private final PagedFile pagedFile;
	private final int freeSpaceInfoSize, blockSize;
	private final Pool<Page> pagePool;
	private final byte[] data;
	Page page;

	@Inject
	public SimpleTransactionManager(final StorableWriter storableWriter,
			final SpaceManagerPolicy spaceManagerPolicy,
			final KlassManager klassManager, final PagedFile pagedFile,
			@Named("freeSpaceInfoSize") final int freeSpaceInfoSize,
			@Named("blockSize") final int blockSize, final Pool<Page> pagePool,
			@Named("maxRecordSize") final int maxRecordSize) {
		this.storableWriter = storableWriter;
		this.spaceManagerPolicy = spaceManagerPolicy;
		this.klassManager = klassManager;
		this.pagedFile = pagedFile;
		this.freeSpaceInfoSize = freeSpaceInfoSize;
		this.blockSize = blockSize;
		this.pagePool = pagePool;

		data = new byte[maxRecordSize];

		page = pagePool.acquire();
	}

	public void performDeleteTransaction(final Storable storable) {
		// TODO Auto-generated method stub

	}

	public boolean performFindTransaction(final Storable storable) {
		if (!klassManager.isKlassManaged(storable.getClassName()))
			return false;

		final Record record = storable.getRecord();

		final Index index = klassManager.getIndexFor(storable.getClassName());
		if (!index.find(record))
			return false;

		readRecord(record, data);

		final SchemaManager schemaManager = klassManager
				.getSchemaManagerFor(storable.getClassName());
		final Schema schema = schemaManager
				.getSchema(record.getSchemaVersion());

		storableWriter.readStorable(data, storable, schema);

		return true;
	}

	public void performInsertTransaction(final Storable storable) {
		final Schema schema = storable.getSchema();
		final int len = storableWriter.writeStorable(data, storable, schema);

		final Record record = storable.getRecord();

		spaceManagerPolicy.acquireRecord(record, len);

		if (!klassManager.isKlassManaged(storable.getClassName()))
			klassManager.addKlass(storable.getClassName());

		final SchemaManager schemaManager = klassManager
				.getSchemaManagerFor(storable.getClassName());
		record.setSchemaVersion(schemaManager.addSchema(schema));

		record.setId(klassManager.getIdFor(storable.getClassName()));

		final Index index = klassManager.getIndexFor(storable.getClassName());
		index.insert(record);

		writeRecord(record, data);

	}

	public void performUpdateTransaction(final Storable storable) {
		final Schema schema = storable.getSchema();
		final int len = storableWriter.writeStorable(data, storable, schema);

		final Record record = storable.getRecord();

		spaceManagerPolicy.reacquireRecord(record, len);

		final SchemaManager schemaManager = klassManager
				.getSchemaManagerFor(storable.getClassName());
		record.setSchemaVersion(schemaManager.addSchema(schema));

		record.setId(klassManager.getIdFor(storable.getClassName()));

		final Index index = klassManager.getIndexFor(storable.getClassName());
		index.insert(record);

		writeRecord(record, data);

	}

	private void readRecord(final Record record, final byte[] data) {
		final int u = record.getPagesUsed();
		int position = 0;

		final Page page = pagePool.acquire();

		for (int i = 0; i < u; i++) {
			final PageUsage usage = record.getPageUsage(i);
			page.setId(usage.pageId);
			pagedFile.readPage(page);

			final byte[] pageData = page.getData();

			for (int j = 0; j < freeSpaceInfoSize; j++)
				for (int k = 0; k < Byte.SIZE; k++)
					if ((usage.usage[j] & 1 << k) != 0) {
						System.arraycopy(pageData, (j * Byte.SIZE + k)
								* blockSize, data, position, blockSize);
						position += blockSize;
					}
		}

		pagePool.release(page);

	}

	private void writeRecord(final Record record, final byte data[]) {
		final int u = record.getPagesUsed();
		int position = 0;

		for (int i = 0; i < u; i++) {
			final PageUsage usage = record.getPageUsage(i);

			page.setId(usage.pageId);
			pagedFile.readPage(page);

			final byte[] pageData = page.getData();

			for (int j = 0; j < freeSpaceInfoSize; j++)
				for (int k = 0; k < Byte.SIZE; k++)
					if ((usage.usage[j] & 1 << k) != 0) {
						System.arraycopy(data, position, pageData, (j
								* Byte.SIZE + k)
								* blockSize, blockSize);

						position += blockSize;
					}

			pagedFile.writePage(page);

		}

	}
}
