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
package org.eclipse.thym.core.engine.internal.cordova;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.client.cache.FileResourceFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.internal.cordova.DownloadableCordovaEngine.LibraryDownloadInfo;
import org.eclipse.thym.core.internal.util.BundleHttpCacheStorage;
import org.eclipse.thym.core.internal.util.HttpUtil;
import org.osgi.framework.BundleContext;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
/**
 * Default implementation for the CordovaEngineRepoProvider extension point.
 * 
 * 
 * @author 
 *
 */
public class DefaultEngineRepoProvider {
	
	private static final String REPO_JSON_URL = "https://raw.githubusercontent.com/eclipse/thym/master/plugins/org.eclipse.thym.core/res/platforms.json";	
	
	
	private InputStream getRemoteJSonStream(){
		BundleContext context = HybridCore.getContext();	
		DefaultHttpClient defHttpClient = new DefaultHttpClient();
		HttpUtil.setupProxy(defHttpClient);
	
		HttpClient client =new CachingHttpClient(defHttpClient,
				new FileResourceFactory(context.getDataFile(BundleHttpCacheStorage.SUBDIR_HTTP_CACHE)), 
				new BundleHttpCacheStorage(HybridCore.getContext().getBundle()), cacheConfig());
		HttpGet get = new HttpGet(REPO_JSON_URL);
		HttpResponse response;
		try{
			response = client.execute(get);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			return stream;
		}catch(IOException e){
			HybridCore.log(IStatus.WARNING, NLS.bind("Could not retrieve the json from remote URL ({0})",REPO_JSON_URL), e );
		}
		return null;
	}
	
	private CacheConfig cacheConfig(){
		CacheConfig config = new CacheConfig();
		config.setMaxObjectSize(120 *1024);
		return config;
	}
	
	/**
	 * Returns a list of all {@link DownloadableCordovaEngine}s that are available from this 
	 * repo.
	 * 
	 * @return
	 * @throws CoreException
	 */
	public List<DownloadableCordovaEngine> getEngines() throws CoreException{		
		ArrayList<DownloadableCordovaEngine> downloadableCordovaEngines = new ArrayList<DownloadableCordovaEngine>();
		try {
			InputStream stream = getRemoteJSonStream();
			Reader r;
			if(stream ==null){
				URL url = FileLocator.find(HybridCore.getContext().getBundle(), new Path("/res/platforms.json"), null);
				if(url == null ){
					throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Could not read downloadable engine list"));
				}
				stream = url.openStream();
			
			}
			r = new InputStreamReader(stream);

			JsonReader reader = new JsonReader(r);
			JsonParser parser = new JsonParser();
			JsonObject root = (JsonObject) parser.parse(reader);
			Set<Entry<String, JsonElement>> versions = root.entrySet();
			for (Iterator<Entry<String, JsonElement>> iterator = versions.iterator(); iterator.hasNext();) {
				Entry<String, JsonElement> entry = iterator.next();
				JsonObject version = entry.getValue().getAsJsonObject();
				DownloadableCordovaEngine engine = new DownloadableCordovaEngine();
				engine.setVersion(entry.getKey());
				Set<Entry<String, JsonElement>> libs = version.entrySet();
				for (Iterator<Entry<String, JsonElement>> libsIterator = libs.iterator(); libsIterator.hasNext();) {
					Entry<String, JsonElement> lib = libsIterator.next();
					LibraryDownloadInfo info = new LibraryDownloadInfo();
					info.setPlatformId(lib.getKey());
					JsonObject infoJsonObj = lib.getValue().getAsJsonObject();
					info.setDownloadURL(infoJsonObj.get("download_url").getAsString());
					info.setVersion(infoJsonObj.get("version").getAsString());
					engine.addLibraryInfo(info);
				}
				downloadableCordovaEngines.add(engine);
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Could not read downloadable engine list",e));
		}
		return downloadableCordovaEngines;
	}	

}
