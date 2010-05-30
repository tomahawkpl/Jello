package com.atteo.jello.schema;

import java.util.ArrayList;

import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.store.ListPage;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class SimpleSchemaManager implements SchemaManager {
	private ArrayList<Schema> schemas;
	private ArrayList<Integer> pageIds;
	private PagedFile pagedFile;
	private ListPage listPage;
	private SpaceManagerPolicy spaceManagerPolicy;

	@Inject
	public SimpleSchemaManager(SpaceManagerPolicy spaceManagerPolicy,
			ListPage listPage, PagedFile pagedFile,
			@Assisted int klassSchemaPageId) {
		this.listPage = listPage;
		this.pagedFile = pagedFile;
		this.spaceManagerPolicy = spaceManagerPolicy;
		
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
		int schemasOnPage = 0;
		int currentPage = 0;
		int l = schemas.size();

		int free = listPage.getCapacity() - 4;
		listPage.position(4);

		for (int i = 0; i < l; i++) {
			Schema schema = schemas.get(i);
			int fieldsLength = schema.fields.length;
			int required = 4;
			String names[] = schema.names;
			int fields[] = schema.fields;

			for (int j = 0; j < fieldsLength; j++)
				required += names[j].length() + 8;

			if (free < required) {
				if (currentPage + 1 >= pageIds.size())
					pageIds.add(spaceManagerPolicy.acquirePage());
				listPage.setNext(pageIds.get(currentPage + 1));
				listPage.setId(pageIds.get(currentPage));
				listPage.reset();
				listPage.putInt(schemasOnPage);
				pagedFile.writePage(listPage);
				currentPage++;
				free = listPage.getCapacity();
				schemasOnPage = 0;
				listPage.position(4);

			}
			
			free -= required;
			schemasOnPage++;
			listPage.putInt(fieldsLength);
			for (int j = 0; j < fieldsLength; j++) {
				listPage.putInt(names[j].length());
				listPage.putString(names[j]);
				listPage.putInt(fields[j]);
			}

		}

		listPage.setNext(ListPage.NO_MORE_PAGES);
		listPage.setId(pageIds.get(currentPage));
		listPage.reset();
		listPage.putInt(schemasOnPage);
		pagedFile.writePage(listPage);

		for (int i = currentPage + 1; i < pageIds.size(); i++) {
			spaceManagerPolicy.releasePage(pageIds.get(i));
			pageIds.remove(i);
		}
	}

	public void create() {
		listPage.setNext(ListPage.NO_MORE_PAGES);
		listPage.setId(pageIds.get(0));
		listPage.reset();
		listPage.putInt(0);
		pagedFile.writePage(listPage);
	}

	public Schema getSchema(int version) {
		if (version < schemas.size())
			return schemas.get(version);
		else
			return null;
	}

	public boolean load() {
		int next;
		listPage.setId(pageIds.get(0));
		pagedFile.readPage(listPage);
		next = listPage.getNext();

		listPage.reset();
		readSchemasFromPage(listPage);

		while (next != ListPage.NO_MORE_PAGES) {
			pageIds.add(next);
			listPage.setId(next);
			pagedFile.readPage(listPage);
			next = listPage.getNext();
			listPage.reset();
			readSchemasFromPage(listPage);
		}

		return false;

	}

	private void readSchemasFromPage(ListPage page) {
		int schemasOnPage = page.getInt();
		int l;
		int nameLen;
		for (int i = 0; i < schemasOnPage; i++) {
			Schema schema = new Schema();
			l = page.getInt();
			schema.fields = new int[l];
			schema.names = new String[l];

			for (int j = 0; j < l; j++) {
				nameLen = page.getInt();
				schema.names[j] = page.getString(nameLen);
				schema.fields[j] = page.getInt();
			}
			schemas.add(schema);
		}
	}

	public void removeSchema(int version) {
		int l = schemas.size();
		for (int i = 0; i < l; i++)
			if (version == schemas.get(i).version) {
				schemas.remove(i);
			}
	}

}
