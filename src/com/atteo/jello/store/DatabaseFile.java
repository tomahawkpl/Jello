package com.atteo.jello.store;

import java.io.File;
import java.io.IOException;

import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;

@Singleton
public class DatabaseFile {
	private PagedFile pagedFile;
	private HeaderPage headerPage;
	private Injector injector;
	
	@Inject
	private DatabaseFile(Injector injector, @Assisted File file, @Assisted boolean readOnly) throws IOException {
		this.injector = injector;
		pagedFile = injector.getInstance(PagedFile.Factory.class).create(file, readOnly);
		pagedFile.open();
		

	}
	
	public void close() {
		try {
			pagedFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadStructure(boolean create) {
		if (create)
			createStructure();
		
	}
	
	private void createStructure() {
		headerPage = injector.getInstance(HeaderPage.class);

		try {
			pagedFile.addPages(1);
		} catch (IOException e) {
			e.printStackTrace();
		}

		pagedFile.writePage(0, headerPage.getData());	
	}
	
	public boolean isValid() {
		return true;
	}
	
	public interface Factory {
		DatabaseFile create(File file, boolean readOnly);
	}
}