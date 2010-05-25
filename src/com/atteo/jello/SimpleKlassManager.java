package com.atteo.jello;

import java.nio.ByteBuffer;
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
	private ByteBuffer buffer;
		
	@Inject
	public SimpleKlassManager(IndexFactory indexFactory, SchemaManagerFactory schemaManagerFactory,
			@Named("klassManagerPageId") int klassManagerPageId,
			ListPage listPage, SpaceManagerPolicy spaceManagerPolicy, PagedFile pagedFile) {
		pageIds = new ArrayList<Integer>();
		pageIds.add(klassManagerPageId);
		this.listPage = listPage;
		buffer = ByteBuffer.wrap(listPage.getData());

		this.spaceManagerPolicy = spaceManagerPolicy;

		this.indexFactory = indexFactory;
		this.schemaManagerFactory = schemaManagerFactory;
		this.pagedFile = pagedFile;
		
		klasses = new ArrayList<KlassInfo>();

	}

	public void addKlass(Class<? extends Storable> klass) {
		KlassInfo klassInfo = new KlassInfo();
		klassInfo.index = null;
		klassInfo.schemaManager = null;
		klassInfo.schemaManagerPageId = spaceManagerPolicy.acquirePage();
		klassInfo.indexPageId = spaceManagerPolicy.acquirePage();
		klassInfo.name = klass.getCanonicalName();
		klasses.add(klassInfo);
	}

	public void commit() {
		int klassesOnPage = 0;
		int currentPage = 0;
		int l = klasses.size();
		int free = listPage.getCapacity();
		for (int i=0;i<l;i++) {
			KlassInfo info = klasses.get(i);
			int nameLength = info.name.length();
			if (free < nameLength + 12) {
				if (currentPage + 1 >= pageIds.size())
					pageIds.add(spaceManagerPolicy.acquirePage());
				listPage.setNext(pageIds.get(currentPage+1));
				listPage.setId(pageIds.get(currentPage));
				buffer.position(0);
				buffer.putInt(klassesOnPage);
				pagedFile.writePage(listPage);
				currentPage++;
				buffer.position(4);
				free = listPage.getCapacity();
				klassesOnPage = 0;
			}
			
			free -= nameLength + 12;
			klassesOnPage++;
			buffer.putInt(nameLength);
			buffer.put(info.name.getBytes());
			buffer.putInt(info.schemaManagerPageId);
			buffer.putInt(info.indexPageId);
		}
		
		listPage.setNext(ListPage.NO_MORE_PAGES);
		listPage.setId(pageIds.get(currentPage));
		buffer.position(0);
		buffer.putInt(klassesOnPage);
		pagedFile.writePage(listPage);
		
		for (int i=currentPage+1;i<pageIds.size();i++) {
			spaceManagerPolicy.releasePage(pageIds.get(i));
			pageIds.remove(i);
		}
	}
	
	public void create() {
		listPage.setNext(ListPage.NO_MORE_PAGES);
		listPage.setId(pageIds.get(0));
		buffer.putInt(0);
		pagedFile.writePage(listPage);
	}

	public SchemaManager getSchemaManagerFor(Class<? extends Storable> klass) {
		int l = klasses.size();
		for (int i = 0; i < l; i++)
			if (klass.getCanonicalName().equals(klasses.get(i).name)) {
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
		int klassesOnPage = buffer.getInt();
		int l;
		for (int i=0;i<klassesOnPage;i++) {
			KlassInfo info = new KlassInfo();
			l = buffer.getInt();
			info.name = new String(listPage.getData(),buffer.position(),l);
			info.schemaManagerPageId = buffer.getInt();
			info.indexPageId = buffer.getInt();
			klasses.add(info);
		}
	}
	
	public void removeKlass(Class<? extends Storable> klass) {
		int l = klasses.size();
		for (int i = 0; i < l; i++)
			if (klass.getCanonicalName().equals(klasses.get(i).name)) {
				klasses.remove(i);
				break;
			}
	}

	public boolean isKlassManaged(Class<? extends Storable> klass) {
		int l = klasses.size();
		for (int i = 0; i < l; i++)
			if (klass.getCanonicalName().equals(klasses.get(i).name))
				return true;
		return false;
	}

	public Index getIndexFor(Class<? extends Storable> klass) {
		int l = klasses.size();
		for (int i = 0; i < l; i++)
			if (klass.getCanonicalName().equals(klasses.get(i).name)) {
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
		SchemaManager schemaManager;
		Index index;
	}

}
