package com.atteo.jello.klass;

import java.util.ArrayList;

import com.atteo.jello.index.Index;
import com.atteo.jello.index.IndexFactory;
import com.atteo.jello.schema.SchemaManager;
import com.atteo.jello.schema.SchemaManagerFactory;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.store.ListPage;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SimpleKlassManager implements KlassManager {
	private final ArrayList<Integer> pageIds;
	private final ArrayList<KlassInfo> klasses;
	private final ListPage listPage;
	private final SpaceManagerPolicy spaceManagerPolicy;
	private final IndexFactory indexFactory;
	private final SchemaManagerFactory schemaManagerFactory;
	private final PagedFile pagedFile;

	@Inject
	public SimpleKlassManager(final IndexFactory indexFactory,
			final SchemaManagerFactory schemaManagerFactory,
			@Named("klassManagerPageId") final int klassManagerPageId,
			final ListPage listPage,
			final SpaceManagerPolicy spaceManagerPolicy,
			final PagedFile pagedFile) {
		pageIds = new ArrayList<Integer>();
		pageIds.add(klassManagerPageId);
		this.listPage = listPage;

		this.spaceManagerPolicy = spaceManagerPolicy;

		this.indexFactory = indexFactory;
		this.schemaManagerFactory = schemaManagerFactory;
		this.pagedFile = pagedFile;

		klasses = new ArrayList<KlassInfo>();

	}

	public void addKlass(final String klassName) {
		final KlassInfo klassInfo = new KlassInfo();
		klassInfo.index = null;
		klassInfo.schemaManager = null;
		klassInfo.schemaManagerPageId = spaceManagerPolicy.acquirePage();
		klassInfo.indexPageId = spaceManagerPolicy.acquirePage();
		klassInfo.name = klassName;
		klassInfo.nextId = 0;
		klasses.add(klassInfo);
	}

	public void commit() {
		int klassesOnPage = 0;
		int currentPage = 0;
		final int l = klasses.size();
		int free = listPage.getCapacity() - 4;
		listPage.position(4);
		for (int i = 0; i < l; i++) {
			final KlassInfo info = klasses.get(i);
			final int nameLength = info.name.length();
			if (free < nameLength + 16) {
				if (currentPage + 1 >= pageIds.size())
					pageIds.add(spaceManagerPolicy.acquirePage());
				listPage.setNext(pageIds.get(currentPage + 1));
				listPage.setId(pageIds.get(currentPage));
				listPage.reset();
				listPage.putInt(klassesOnPage);
				pagedFile.writePage(listPage);
				currentPage++;
				listPage.position(4);
				free = listPage.getCapacity();
				klassesOnPage = 0;
			}

			free -= nameLength + 16;
			klassesOnPage++;
			listPage.putInt(nameLength);
			listPage.putString(info.name);
			listPage.putInt(info.schemaManagerPageId);
			listPage.putInt(info.indexPageId);
			listPage.putInt(info.nextId);
		}

		listPage.setNext(ListPage.NO_MORE_PAGES);
		listPage.setId(pageIds.get(currentPage));
		listPage.position(0);
		listPage.putInt(klassesOnPage);
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

	public int getIdFor(final String klassName) {
		final int l = klasses.size();
		for (int i = 0; i < l; i++) {
			final KlassInfo storedKlass = klasses.get(i);
			if (klassName.equals(storedKlass.name)) {
				storedKlass.nextId++;
				return storedKlass.nextId - 1;
			}
		}
		throw new IllegalArgumentException("Class is not managed");
	}

	public Index getIndexFor(final String klassName) {
		final int l = klasses.size();
		for (int i = 0; i < l; i++)
			if (klassName.equals(klasses.get(i).name)) {
				if (klasses.get(i).index == null)
					klasses.get(i).index = indexFactory
							.create(klasses.get(i).indexPageId);
				return klasses.get(i).index;
			}

		return null;
	}

	public SchemaManager getSchemaManagerFor(final String klassName) {
		final int l = klasses.size();
		for (int i = 0; i < l; i++)
			if (klassName.equals(klasses.get(i).name)) {
				if (klasses.get(i).schemaManager == null)
					klasses.get(i).schemaManager = schemaManagerFactory
							.create(klasses.get(i).indexPageId);
				return klasses.get(i).schemaManager;
			}

		return null;
	}

	public boolean isKlassManaged(final String klassName) {
		final int l = klasses.size();
		for (int i = 0; i < l; i++)
			if (klassName.equals(klasses.get(i).name))
				return true;
		return false;
	}

	public boolean load() {
		int next;
		listPage.setId(pageIds.get(0));
		pagedFile.readPage(listPage);
		next = listPage.getNext();
		readKlassesFromPage(listPage);

		while (next != ListPage.NO_MORE_PAGES) {
			pageIds.add(next);
			listPage.setId(next);
			pagedFile.readPage(listPage);
			next = listPage.getNext();
			readKlassesFromPage(listPage);

		}

		return false;
	}

	public void removeKlass(final String klassName) {
		final int l = klasses.size();
		for (int i = 0; i < l; i++)
			if (klassName.equals(klasses.get(i).name)) {
				klasses.remove(i);
				break;
			}
	}

	private void readKlassesFromPage(final ListPage page) {
		page.reset();
		final int klassesOnPage = page.getInt();
		int l;
		for (int i = 0; i < klassesOnPage; i++) {
			final KlassInfo info = new KlassInfo();
			l = page.getInt();
			info.name = page.getString(l);
			info.schemaManagerPageId = page.getInt();
			info.indexPageId = page.getInt();
			info.nextId = page.getInt();
			klasses.add(info);
		}
	}

	class KlassInfo {
		String name;
		int schemaManagerPageId;
		int indexPageId;
		int nextId;
		SchemaManager schemaManager;
		Index index;
	}

}
