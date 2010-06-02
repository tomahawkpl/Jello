package com.atteo.jello.index;

import android.util.Pool;

import com.atteo.jello.store.Page;
import com.google.inject.Inject;

public class PagePoolProxy {
	
	private Pool<Page> pagePool;
	
	@Inject
	PagePoolProxy(Pool<Page> pagePool) {
		this.pagePool = pagePool;
	}
	
	Page acquire() {
		return pagePool.acquire();
	}
	
	void release(Page page) {
		pagePool.release(page);
	}
}
