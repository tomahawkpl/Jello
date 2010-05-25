package com.atteo.jello.schema;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.atteo.jello.store.ListPage;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class SimpleSchemaManager implements SchemaManager {
	private ArrayList<Schema> schemas;
	private ArrayList<Integer> pageIds;
	private PagedFile pagedFile;
	private ByteBuffer buffer;
	private ListPage listPage;

	@Inject
	public SimpleSchemaManager(ListPage listPage, PagedFile pagedFile,
			@Assisted int klassSchemaPageId) {
		this.listPage = listPage;
		this.pagedFile = pagedFile;
		buffer = ByteBuffer.wrap(listPage.getData());

		pageIds = new ArrayList<Integer>();
		pageIds.add(klassSchemaPageId);
		
		schemas = new ArrayList<Schema>();
	}

	public int addSchema(Schema schema) {
		int l = schemas.size();
		for (int i = 0; i < l; i++)
			if (schema.equals(schemas.get(i)))
				return schemas.get(i).version;

		schema.version = schemas.size();
		schemas.add(schema);
		return schemas.size() - 1;
	}

	public void commit() {

	}

	public void create() {
		listPage.setNext(ListPage.NO_MORE_PAGES);
		listPage.setId(pageIds.get(0));
		buffer.putInt(0);
		pagedFile.writePage(listPage);
	}

	public Schema getSchema(int version) {
		return schemas.get(version);
	}

	public void load() {
		// TODO Auto-generated method stub

	}

	public void removeSchema(int version) {
		int l = schemas.size();
		for (int i = 0; i < l; i++)
			if (version == schemas.get(i).version) {
				schemas.remove(i);
			}
	}

}
