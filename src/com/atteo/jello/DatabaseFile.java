package com.atteo.jello;

import com.atteo.jello.space.SpaceManagerPolicy;
import com.atteo.jello.store.HeaderPage;
import com.atteo.jello.store.PagedFile;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

@Singleton
public class DatabaseFile implements Module {
	private final PagedFile pagedFile;
	private HeaderPage headerPage;
	private SpaceManagerPolicy spaceManagerPolicy;

	private KlassManager klassManager;
	private final Injector injector;

	private int headerPageId;
	private int minimumPages;

	@Inject
	private DatabaseFile(final Injector injector, final PagedFile pagedFile,
			@Named("headerPageId") int headerPageId,
			@Named("minimumPages") int minimumPages) {

		this.injector = injector;
		this.pagedFile = pagedFile;
		this.headerPageId = headerPageId;
		this.minimumPages = minimumPages;
	}

	public void close() {
		pagedFile.close();
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

	public boolean createStructure() {
		if (pagedFile.addPages(minimumPages) != minimumPages - 1)
			return false;

		headerPage = injector.getInstance(HeaderPage.class);
		headerPage.setId(headerPageId);
		pagedFile.writePage(headerPage);

		spaceManagerPolicy = injector.getInstance(SpaceManagerPolicy.class);
		spaceManagerPolicy.create();

		klassManager = injector.getInstance(KlassManager.class);
		klassManager.create();

		return true;
	}

	public void configure(Binder binder) {
		binder.bind(Short.class).annotatedWith(Names.named("pageSize"))
				.toInstance(headerPage.getPageSize());
		binder.bind(Short.class).annotatedWith(Names.named("blockSize"))
				.toInstance(headerPage.getBlockSize());
		binder.bind(Integer.class).annotatedWith(
				Names.named("freeSpaceInfoPageId")).toInstance(
				headerPage.getFreeSpaceInfoPageId());
		binder.bind(Integer.class).annotatedWith(
				Names.named("klassManagerPageId")).toInstance(
				headerPage.getKlassManagerPageId());
		binder.bind(Integer.class).annotatedWith(
				Names.named("fileFormatVersion")).toInstance(
				headerPage.getFileFormatVersion());
		
		int mp = headerPage.getFreeSpaceInfoPageId();
		if (mp < headerPage.getKlassManagerPageId())
			mp = headerPage.getKlassManagerPageId();
		mp++;
		
		binder.bind(Integer.class).annotatedWith(
				Names.named("minimumPages")).toInstance(mp);

	}
}
