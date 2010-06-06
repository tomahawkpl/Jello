package com.atteo.jello;

import java.util.HashMap;

import com.atteo.jello.klass.KlassManager;
import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.store.HeaderPage;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class DatabaseFile {
	private final PagedFile pagedFile;
	private HeaderPage headerPage;
	private SpaceManagerPolicy spaceManagerPolicy;

	private KlassManager klassManager;
	private final Injector injector;

	private final int headerPageId;
	private final int minimumPages;

	@Inject
	private DatabaseFile(final Injector injector, final PagedFile pagedFile,
			@Named("headerPageId") final int headerPageId,
			@Named("minimumPages") final int minimumPages) {

		this.injector = injector;
		this.pagedFile = pagedFile;
		this.headerPageId = headerPageId;
		this.minimumPages = minimumPages;
	}

	public void close() {
		pagedFile.close();
	}

	public HashMap<String, String> getReadProperties() {
		int mp = headerPage.getFreeSpaceInfoPageId();
		if (mp < headerPage.getKlassManagerPageId())
			mp = headerPage.getKlassManagerPageId();
		mp++;

		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put("pageSize", String.valueOf(headerPage.getPageSize()));
		properties.put("blockSize", String.valueOf(headerPage.getBlockSize()));
		properties.put("freeSpaceInfoPageId", String.valueOf(headerPage.getFreeSpaceInfoPageId()));
		properties.put("klassManagerPageId", String.valueOf(headerPage.getKlassManagerPageId()));
		properties.put("fileFormatVersion", String.valueOf(headerPage.getFileFormatVersion()));
		properties.put("minimumPages", String.valueOf(mp));

		return properties;
	}

	public boolean createStructure() {
		if (pagedFile.addPages(minimumPages) != minimumPages - 1)
			return false;

		headerPage = injector.getInstance(HeaderPage.class);
		headerPage.setId(headerPageId);
		pagedFile.writePage(headerPage);

		spaceManagerPolicy = injector.getInstance(SpaceManagerPolicy.class);
		spaceManagerPolicy.create();
		for (int i = 0; i < minimumPages; i++)
			spaceManagerPolicy.setPageUsed(i, true);
		
		klassManager = injector.getInstance(KlassManager.class);
		klassManager.create();

		return true;
	}

	public PagedFile getPagedFile() {
		return pagedFile;
	}

	public SpaceManagerPolicy getSpaceManagerPolicy() {
		return spaceManagerPolicy;
	}

	public boolean loadHeader() {
		if (pagedFile.getPageCount() < minimumPages)
			return false;

		headerPage = injector.getInstance(HeaderPage.class);

		headerPage.setId(headerPageId);
		pagedFile.readPage(headerPage);
		if (!headerPage.load())
			return false;

		return true;
	}

	public boolean loadStructure() {
		spaceManagerPolicy = injector.getInstance(SpaceManagerPolicy.class);
		if (!spaceManagerPolicy.load())
			return false;
		klassManager = injector.getInstance(KlassManager.class);
		if (!klassManager.load())
			return false;

		return true;
	}

	/**
	 * Either loadStructure or createStructure must be called in order for
	 * isValid to return true
	 * 
	 * @return
	 */
	public int open() {
		return pagedFile.open();
	}
}
