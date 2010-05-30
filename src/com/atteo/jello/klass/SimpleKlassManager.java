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
	private ArrayList<Integer> pageIds;
	private ArrayList<KlassInfo> klasses;
	private ListPage listPage;
	private SpaceManagerPolicy spaceManagerPolicy;
	private IndexFactory indexFactory;
	private SchemaManagerFactory schemaManagerFactory;
	private PagedFile pagedFile;
	
	@Inject
	public SimpleKlassManager(IndexFactory indexFactory, SchemaManagerFactory schemaManagerFactory,
			@Named("klassManagerPageId") int klassManagerPageId,
			ListPage listPage, SpaceManagerPolicy spaceManagerPolicy, PagedFile pagedFile) {
		pageIds = new ArrayList<Integer>();
		pageIds.add(klassManagerPageId);
		this.listPage = listPage;

		this.spaceManagerPolicy = spaceManagerPolicy;

		this.indexFactory = indexFactory;
		this.schemaManagerFactory = schemaManagerFactory;
		this.pagedFile = pagedFile;
		
		klasses = new ArrayList<KlassInfo>();

	}

	public void addKlass(String klassName) {
		KlassInfo klassInfo = new KlassInfo();
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
		int l = klasses.size();
		int free = listPage.getCapacity() - 4;
		listPage.position(4);
		for (int i=0;i<l;i++) {
			KlassInfo info = klasses.get(i);
			int nameLength = info.name.length();
			if (free < nameLength + 16) {
				if (currentPage + 1 >= pageIds.size())
					pageIds.add(spaceManagerPolicy.acquirePage());
				listPage.setNext(pageIds.get(currentPage+1));
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
		
		for (int i=currentPage+1;i<pageIds.size();i++) {
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

	public SchemaManager getSchemaManagerFor(String klassName) {
		int l = klasses.size();
		for (int i = 0; i < l; i++)
			if (klassName.equals(klasses.get(i).name)) {
				if (klasses.get(i).schemaManager == null)
					klasses.get(i).schemaManager = schemaManagerFactory.create(klasses.get(i).indexPageId);
				return klasses.get(i).schemaManager;
			}
				
		return null;
	}

	public boolean load() {
		int next;
		listPage.setId(pageIds.get(0));
		pagedFile.readPage(listPage);
		next = listPage.getNext();
		readKlassesFromPage(listPage);
		
		while(next != ListPage.NO_MORE_PAGES) {
			pageIds.add(next);
			listPage.setId(next);
			pagedFile.readPage(listPage);
			next = listPage.getNext();
			readKlassesFromPage(listPage);
			
		}
		
		return false;
	}

	private void readKlassesFromPage(ListPage page) {
		page.reset();
		int klassesOnPage = page.getInt();
		int l;
		for (int i=0;i<klassesOnPage;i++) {
			KlassInfo info = new KlassInfo();
			l = page.getInt();
			info.name = page.getString(l);
			info.schemaManagerPageId = page.getInt();
			info.indexPageId = page.getInt();
			info.nextId = page.getInt();
			klasses.add(info);
		}
	}
	
	public void removeKlass(String klassName) {
		int l = klasses.size();
		for (int i = 0; i < l; i++)
			if (klassName.equals(klasses.get(i).name)) {
				klasses.remove(i);
				break;
			}
	}

	public boolean isKlassManaged(String klassName) {
		int l = klasses.size();
		for (int i = 0; i < l; i++)
			if (klassName.equals(klasses.get(i).name))
				return true;
		return false;
	}

	public Index getIndexFor(String klassName) {
		int l = klasses.size();
		for (int i = 0; i < l; i++)
			if (klassName.equals(klasses.get(i).name)) {
				if (klasses.get(i).index == null)
					klasses.get(i).index = indexFactory.create(klasses.get(i).indexPageId);
				return klasses.get(i).index;
			}
				
		return null;
	}

	class KlassInfo {
		String name;
		int schemaManagerPageId;
		int indexPageId;
		int nextId;
		SchemaManager schemaManager;
		Index index;
	}

	public int getIdFor(String klassName) {
		int l = klasses.size();
		for (int i = 0; i < l; i++) {
			KlassInfo storedKlass = klasses.get(i);
			if (klassName.equals(storedKlass.name)) {
				storedKlass.nextId++;
				return storedKlass.nextId - 1;
			}
		}
		throw new IllegalArgumentException("Class is not managed");
	}

}
