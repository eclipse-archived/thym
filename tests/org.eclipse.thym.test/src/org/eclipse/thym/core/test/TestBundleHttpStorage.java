/*******************************************************************************
 * Copyright (c) 2013, 2014 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.test;

import java.io.IOException;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.cache.HttpCacheEntry;
import org.apache.http.client.cache.Resource;
import org.apache.http.impl.client.cache.HeapResource;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.eclipse.thym.core.internal.util.BundleHttpCacheStorage;
import org.eclipse.thym.hybrid.test.Activator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction") //test
public class TestBundleHttpStorage {
	
	private BundleHttpCacheStorage cacheStorage;
	
	@Before
	public  void setUp(){
		cacheStorage = new BundleHttpCacheStorage(Activator.getDefault().getBundle());
	}
	
	@Test
	public void testCachePutAndRetrieve() throws IOException{
		HttpCacheEntry entry = makeHttpCacheEntry();
		cacheStorage.putEntry("foo", entry);
		assertNotNull(cacheStorage.getEntry("foo"));
	}

	public void testCachePutAndRemove() throws IOException{
		HttpCacheEntry entry = makeHttpCacheEntry();
		cacheStorage.putEntry("foo", entry);
		assertNotNull(cacheStorage.getEntry("foo"));
		cacheStorage.removeEntry("foo");
		assertNull(cacheStorage.getEntry("foo"));
	}
	
	private HttpCacheEntry makeHttpCacheEntry() {
		final Date now = new Date();
	    final StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
	    final Header[] headers = {
	                new BasicHeader("Date", DateUtils.formatDate(now)),
	                new BasicHeader("Server", "MockServer/1.0")
	     };
	    final Resource resource = new HeapResource(new byte[0]);
		HttpCacheEntry entry = new HttpCacheEntry(now, now, statusLine, headers, resource);
		return entry;
	}
	

}
