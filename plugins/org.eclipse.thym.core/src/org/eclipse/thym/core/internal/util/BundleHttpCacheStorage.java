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
package org.eclipse.thym.core.internal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.cache.HttpCacheEntry;
import org.apache.http.client.cache.HttpCacheStorage;
import org.apache.http.client.cache.HttpCacheUpdateCallback;
import org.apache.http.client.cache.HttpCacheUpdateException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.core.HybridCore;
import org.osgi.framework.Bundle;


/**
 * A cache storage whose back-end is on OSGi bundles data file area.
 * 
 * @author Gorkem Ercan
 *
 */
public class BundleHttpCacheStorage implements HttpCacheStorage {
	public static final String SUBDIR_HTTP_CACHE = "httpCache";
	private File cacheDir;
	
	
	public BundleHttpCacheStorage(Bundle bundle) {
		File f = bundle.getDataFile(SUBDIR_HTTP_CACHE);
		if(!f.exists()){
			f.mkdir();
		}
		cacheDir = f;
	}

	@Override
	public void putEntry(String key, HttpCacheEntry entry) throws IOException {
		ByteArrayOutputStream byteArrayOS = null;
		ObjectOutputStream objectOut = null;
			
		try{
			File f = getCacheFile(key);
			byteArrayOS = new ByteArrayOutputStream();
			objectOut = new ObjectOutputStream(byteArrayOS);
			objectOut.writeObject(entry);
			objectOut.flush();
			FileUtils.writeByteArrayToFile(f,byteArrayOS.toByteArray());
		}finally{
			if(objectOut != null )
				objectOut.close();
			if(byteArrayOS != null)
				byteArrayOS.close();
		}
	}

	private File getCacheFile(String key) {
		String fileName = Integer.toHexString(key.hashCode());
		File f = new File(cacheDir, fileName);
		return f;
	}

	@Override
	public HttpCacheEntry getEntry(String key) throws IOException {
		File f = getCacheFile(key);
		if(f.exists()){
			ByteArrayInputStream bais = null;
			ObjectInputStream ois = null;
			HttpCacheEntry entry = null;

			try {
				byte[] bytes = FileUtils.readFileToByteArray(f);
				bais = new ByteArrayInputStream(bytes);
				ois = new ObjectInputStream(bais);
				entry = (HttpCacheEntry) ois.readObject();
			} catch (ClassNotFoundException e) {
				HybridCore.log(IStatus.ERROR, "Missing bundle", e);
			}
			finally{
				if(ois != null )
					ois.close();
				if(bais != null )
					bais.close();
			}
			return entry;	
		}
		return null;
	}

	@Override
	public void removeEntry(String key) throws IOException {
		File f = getCacheFile(key);
		FileUtils.deleteQuietly(f);
	}

	@Override
	public void updateEntry(String key, HttpCacheUpdateCallback callback)
			throws IOException, HttpCacheUpdateException {
		HttpCacheEntry existing = getEntry(key);
		HttpCacheEntry updated = callback.update(existing);
		if(updated == null ){
			removeEntry(key);
		}else{
			putEntry(key, updated);
		}
	}
}
		
