/*******************************************************************************
 * Copyright (c) 2013, 2015 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.plugin.registry;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.client.cache.HeapResourceFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.filetransfer.IncomingFileTransferException;
import org.eclipse.ecf.filetransfer.identity.FileCreateException;
import org.eclipse.ecf.filetransfer.identity.FileIDFactory;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.internal.util.BundleHttpCacheStorage;
import org.eclipse.thym.core.internal.util.HttpUtil;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPlugin.RegistryPluginVersion;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class CordovaPluginRegistryManager {
	
	private static final String REGISTRY_URL = "http://registry.npmjs.org/";
//    private static final String PLUGIN_LIST_URL = 
	
	private final File cacheHome;
	private HashMap<String, CordovaRegistryPlugin> detailedPluginInfoCache = new HashMap<String, CordovaRegistryPlugin>();
	
	public CordovaPluginRegistryManager() {
		cacheHome = new File(FileUtils.getUserDirectory(), ".plugman"+File.separator+"cache");
	}
	
	public CordovaRegistryPlugin getCordovaPluginInfo(String name) throws CoreException {
		
		CordovaRegistryPlugin plugin = detailedPluginInfoCache.get(name);
		if(plugin != null )
			return plugin;
		DefaultHttpClient defHttpClient = new DefaultHttpClient();
		HttpUtil.setupProxy(defHttpClient);
		HttpClient client =new CachingHttpClient(defHttpClient,
				new HeapResourceFactory(), 
				new BundleHttpCacheStorage(HybridCore.getContext().getBundle()), getCacheConfig()); 
		
		HttpGet get = new HttpGet(REGISTRY_URL+name);
		HttpResponse response;
		try {
			response = client.execute(get);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			JsonReader reader = new JsonReader(new InputStreamReader(stream));
			plugin = new CordovaRegistryPlugin();
			readPluginInfo(reader, plugin);
			this.detailedPluginInfoCache.put(name, plugin);
			return plugin;
		} catch (ClientProtocolException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Can not retrieve plugin information for " + name, e));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Can not retrieve plugin information for " + name, e));
		}
	}
	
	/**
	 * Returns a directory where the given version of the Cordova Plugin 
	 * can be installed from. This method downloads the given 
	 * cordova plugin if necessary.
	 * 
	 * @param plugin
	 * @return
	 */
	public File getInstallationDirectory( RegistryPluginVersion plugin, IProgressMonitor monitor ){
		if(monitor == null )
			monitor = new NullProgressMonitor();
		
		File pluginDir = getFromCache(plugin);
		if (pluginDir != null ){
			return pluginDir;
		}
		File newCacheDir = calculateCacheDir(plugin);
		
		IRetrieveFileTransfer transfer = HybridCore.getDefault().getFileTransferService();
		IFileID remoteFileID;
		
		try {
			remoteFileID = FileIDFactory.getDefault().createFileID(transfer.getRetrieveNamespace(), plugin.getTarball());
			Object lock = new Object();
			PluginReceiver receiver = new PluginReceiver(newCacheDir,monitor, lock);
		    synchronized (lock) {
		    	transfer.sendRetrieveRequest(remoteFileID, receiver, null);
		    	lock.wait();
			}
		} catch (FileCreateException e) {
			HybridCore.log(IStatus.ERROR, "Cordova plugin fetch error", e);
		} catch (IncomingFileTransferException e) {
			HybridCore.log(IStatus.ERROR, "Cordova plugin fetch error", e);
		} catch (InterruptedException e) {
			HybridCore.log(IStatus.ERROR, "Cordova plugin fetch error", e);
		}
		return new File(newCacheDir, "package");
	}
	
	private File getFromCache( RegistryPluginVersion plugin ){
		File cachedPluginDir = calculateCacheDir(plugin);
		File packageDir = new File(cachedPluginDir,"package");
		if( !packageDir.isDirectory()){
			return null;
		}
		File pluginxml = new File(packageDir, PlatformConstants.FILE_XML_PLUGIN);
		if(cachedPluginDir.isDirectory() && pluginxml.exists())
			return packageDir;
		return null;
	}

	private File calculateCacheDir(RegistryPluginVersion plugin) {
		File cachedPluginDir = new File(this.cacheHome, plugin.getName() + File.separator +
				plugin.getVersionNumber());
				
		return cachedPluginDir;
	}
	
	private static CacheConfig getCacheConfig(){
		CacheConfig config = new CacheConfig();
		config.setMaxObjectSize(120 *1024);
		return config;
	}
	
	public List<CordovaRegistryPluginInfo> retrievePluginInfos(IProgressMonitor monitor) throws CoreException
	{
		
		if(monitor == null )
			monitor = new NullProgressMonitor();
		
		monitor.beginTask("Retrieve plug-in registry catalog", 10);
		DefaultHttpClient theHttpClient = new DefaultHttpClient();
		HttpUtil.setupProxy(theHttpClient);
		HttpClient client = new CachingHttpClient(theHttpClient, 
				new HeapResourceFactory(), 
				new BundleHttpCacheStorage(HybridCore.getContext().getBundle()), getCacheConfig());
		JsonReader reader= null;
		try {
			if(monitor.isCanceled()){
				return null;
			}
			String url = REGISTRY_URL + "-/_view/byKeyword?startkey=%5B%22ecosystem:cordova%22%5D&endkey=%5B%22ecosystem:cordova1%22%5D&group_level=3";
			HttpGet get = new HttpGet(URI.create(url));
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			monitor.worked(7);
			reader = new JsonReader(new InputStreamReader(stream));
			reader.beginObject();//start the Registry
			final ArrayList<CordovaRegistryPluginInfo> plugins = new ArrayList<CordovaRegistryPluginInfo>();
			while(reader.hasNext()){
				JsonToken token = reader.peek();
				switch (token) {
				case BEGIN_ARRAY: 
					reader.beginArray();
					break;
				case BEGIN_OBJECT: 
					plugins.add(parseCordovaRegistryPluginInfo(reader));
					break;
				default:
					reader.skipValue();
					break;
				}
				
			}
			return plugins;

		} catch (ClientProtocolException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Can not retrieve plugin catalog",e));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Can not retrieve plugin catalog", e));
		}finally{
			if(reader != null )
				try {
					reader.close();
				} catch (IOException e) { /*ignored*/ }
			monitor.done();
		}
	}
	
	private CordovaRegistryPluginInfo parseCordovaRegistryPluginInfo(JsonReader reader) throws IOException{
		CordovaRegistryPluginInfo info = new CordovaRegistryPluginInfo();
		reader.beginObject();
		reader.skipValue(); // name
		reader.beginArray();
		reader.nextString(); //ecosystem:cordova
		info.setName(safeReadStringValue(reader));
		info.setDescription(safeReadStringValue(reader));
		reader.endArray();
		reader.nextName();reader.nextInt();
		reader.endObject();
		return info;
	}
	
	private String safeReadStringValue(JsonReader reader) throws IOException{
		if(reader.peek() == JsonToken.STRING){
			return reader.nextString();
		}
		reader.skipValue();
		return "";
	}

	private void readVersionInfo(JsonReader reader, RegistryPluginVersion version)throws IOException{
		Assert.isNotNull(version);
		reader.beginObject();
		while(reader.hasNext()){
			JsonToken token = reader.peek();
			switch (token) {
			case NAME:
				String name = reader.nextName();
				if("dist".equals(name)){
					parseDistDetails(reader,  version);
					break;
				}
				break;

			default:
				reader.skipValue();
				break;
			}
		}
		reader.endObject();
	}
	
	private void readPluginInfo(JsonReader reader, CordovaRegistryPlugin plugin ) throws IOException {
		Assert.isNotNull(plugin);
		reader.beginObject();

		while (reader.hasNext()) {
			JsonToken token = reader.peek();
			switch (token) {
			case NAME: {
				String name = reader.nextName();
				if ("name".equals(name)) {
					plugin.setName(reader.nextString());
					break;
				}
				if ("description".equals(name)) {
					plugin.setDescription(reader.nextString());
					break;
				}
				if ("keywords".equals(name)) {
					parseKeywords(reader, plugin);
					break;
				}
				if("maintainers".equals(name)){
					parseMaintainers(reader,plugin);
					break;
				}
				if("dist-tags".equals(name)){
					parseLatestVersion(reader,plugin);
					break;
				}
				if("versions".equals(name) ){ 
					parseVersions(reader, plugin);       
					break;
				}
				if("license".equals(name)){
					plugin.setLicense(reader.nextString());
					break;
				}
				break;
			}

			default:
				reader.skipValue();
				break;
			}
		}
		reader.endObject();
	}

	private void parseDistDetails(JsonReader reader, RegistryPluginVersion plugin) throws IOException{
		reader.beginObject();
		JsonToken token = reader.peek();
		while(token != JsonToken.END_OBJECT){
			switch (token) {
			case NAME:
				String name = reader.nextName();
				if("shasum".equals(name)){
					plugin.setShasum(reader.nextString());
					break;
				}
				if("tarball".equals(name)){
					plugin.setTarball(reader.nextString());
					break;
				}
				break;

			default:
				reader.skipValue();
				break;
			}
			token = reader.peek();
		}
		reader.endObject();
	}

	private void parseVersions(JsonReader reader,
			CordovaRegistryPlugin plugin) throws IOException{
		reader.beginObject();//versions
		JsonToken token = reader.peek();
		while( token != JsonToken.END_OBJECT ){
			switch (token) {
			case NAME:
				RegistryPluginVersion version = plugin.new RegistryPluginVersion();
				version.setVersionNumber(reader.nextName());
				readVersionInfo(reader, version);
				plugin.addVersion(version);
				break;

			default:
				reader.skipValue();
				break;
			}
			token = reader.peek();
		}
		reader.endObject();
	}

	private void parseLatestVersion(JsonReader reader, CordovaRegistryPlugin plugin) throws IOException{
		reader.beginObject();
		JsonToken token = reader.peek();
		while ( token != JsonToken.END_OBJECT){
			switch (token) {
			case NAME:
				String tag = reader.nextName();
				if("latest".equals(tag)){
					plugin.setLatestVersion(reader.nextString());
				}
				break;

			default:
				reader.skipValue();
				break;
			}
			token = reader.peek();
		}
		reader.endObject();
	}

	private void parseMaintainers(JsonReader reader, CordovaRegistryPlugin plugin) throws IOException{
		reader.beginArray();
		String name=null, email = null;
		JsonToken token = reader.peek();
		
		while( token != JsonToken.END_ARRAY ){
			switch (token) {
			case BEGIN_OBJECT:
				reader.beginObject();
				name = email = null;
				break;
			case END_OBJECT:
				reader.endObject();
				plugin.addMaintainer(email, name);
				break;
			case NAME:
				String tagName = reader.nextName();
				if("name".equals(tagName)){
					name = reader.nextString();
					break;
				}
				if("email".equals(tagName)){
					email = reader.nextString();
					break;
				}
			default:
				Assert.isTrue(false, "Unexpected token");
				break;
			}
			token =reader.peek();
		}
		reader.endArray();
	}

	private void parseKeywords(JsonReader reader, CordovaRegistryPlugin plugin) throws IOException{
		reader.beginArray();
		while(reader.hasNext()){
			plugin.addKeyword(reader.nextString());
		}
		reader.endArray();
	}

}
