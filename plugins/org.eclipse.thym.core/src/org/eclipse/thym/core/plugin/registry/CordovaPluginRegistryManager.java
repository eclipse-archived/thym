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
package org.eclipse.thym.core.plugin.registry;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.client.cache.FileResourceFactory;
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
import org.osgi.framework.BundleContext;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class CordovaPluginRegistryManager {
	
	private static final String REGISTRY_CLIENT_ID = "eclipseTHyM";
	public static final String DEFAULT_REGISTRY_URL = "http://registry.cordova.io/";
	private long updated;
	private List<CordovaRegistryPluginInfo> plugins;
	private String registry;
	private final File cacheHome;
	private HashMap<String, CordovaRegistryPlugin> detailedPluginInfoCache = new HashMap<String, CordovaRegistryPlugin>();
	
	public CordovaPluginRegistryManager(String url) {
		this.registry = url;
		cacheHome = new File(FileUtils.getUserDirectory(), ".plugman"+File.separator+"cache");
	}
	
	public CordovaRegistryPlugin getCordovaPluginInfo(String name) throws CoreException {
		
		CordovaRegistryPlugin plugin = detailedPluginInfoCache.get(name);
		if(plugin != null )
			return plugin;
		BundleContext context = HybridCore.getContext();	
		DefaultHttpClient defHttpClient = new DefaultHttpClient();
		HttpUtil.setupProxy(defHttpClient);
		HttpClient client =new CachingHttpClient(defHttpClient,
				new FileResourceFactory(context.getDataFile(BundleHttpCacheStorage.SUBDIR_HTTP_CACHE)), 
				new BundleHttpCacheStorage(HybridCore.getContext().getBundle()), getCacheConfig()); 
		
		String url = registry.endsWith("/") ? registry + name : registry + "/"
				+ name;
		HttpGet get = new HttpGet(url);
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
	public File getInstallationDirectory( CordovaRegistryPluginVersion plugin, IProgressMonitor monitor ){
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
			remoteFileID = FileIDFactory.getDefault().createFileID(transfer.getRetrieveNamespace(), plugin.getDistributionTarball());
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
		updateDownlodCounter(plugin.getName());
		return new File(newCacheDir, "package");
	}
	
	private void updateDownlodCounter(String pluginId) {
		if(!registry.contains("registry.cordova.io"))//ping only cordova registry
			return;
		
		DefaultHttpClient client = new DefaultHttpClient();
		HttpUtil.setupProxy(client);
		String url = registry.endsWith("/") ? registry+"downloads" : registry+"/downloads";
		HttpPost post = new HttpPost(url);
		Date now =new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM.dd");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		JsonObject obj = new JsonObject();
		obj.addProperty("day", df.format(now));
		obj.addProperty("pkg", pluginId);
		obj.addProperty("client", REGISTRY_CLIENT_ID );
		Gson gson = new Gson();
		String json = gson.toJson(obj);
		StringEntity entity;
		try {
			entity = new StringEntity(json);
			entity.setContentType("application/json");
			post.setEntity(entity);
			HttpResponse response = client.execute(post);
			if (response.getStatusLine().getStatusCode() != 201) {
				HybridCore.log(IStatus.INFO, "Unable to ping Cordova registry download counter", null);
			}
		} catch (UnsupportedEncodingException e) {
			HybridCore.log(IStatus.INFO, "Unable to ping Cordova registry download counter", e);
		} catch (IOException e) {
			HybridCore.log(IStatus.INFO, "Unable to ping Cordova registry download counter", e);
		}
	}

	private File getFromCache( CordovaRegistryPluginVersion plugin ){
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

	private File calculateCacheDir(CordovaRegistryPluginVersion plugin) {
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
		BundleContext context = HybridCore.getContext();
		DefaultHttpClient theHttpClient = new DefaultHttpClient();
		HttpUtil.setupProxy(theHttpClient);
		HttpClient client = new CachingHttpClient(theHttpClient, 
				new FileResourceFactory(context.getDataFile(BundleHttpCacheStorage.SUBDIR_HTTP_CACHE)), 
				new BundleHttpCacheStorage(HybridCore.getContext().getBundle()), getCacheConfig());
		String url = registry.endsWith("/") ? registry+"-/all" : registry+"/-/all";
		HttpGet get = new HttpGet(url);
		HttpResponse response;
		
		try {
			if(monitor.isCanceled()){
				return null;
			}
			response = client.execute(get);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			monitor.worked(7);
			JsonReader reader = new JsonReader(new InputStreamReader(stream));
			reader.beginObject();//start the Registry
			plugins = new ArrayList<CordovaRegistryPluginInfo>();
			while(reader.hasNext()){
				JsonToken token = reader.peek();
				switch (token) {
				case BEGIN_OBJECT:
					CordovaRegistryPluginInfo info = new CordovaRegistryPluginInfo();
					readPluginInfo(reader, info);
					plugins.add(info);
					break;
				case NAME:
					String name = reader.nextName();
					if(name.equals("_updated")){
						long newUpdate = reader.nextLong();
						if(newUpdate == this.updated){//No changes 
							return plugins;
						}
						
					}
					break;
				default:
					Assert.isTrue(false, "Unexpected token: " + token);
					break;
				}
				
			}
			reader.endObject();
			return plugins;

		} catch (ClientProtocolException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Can not retrieve plugin catalog",e));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Can not retrieve plugin catalog", e));
		}finally{
			monitor.done();
		}
		
	}

	private void readPluginInfo(JsonReader reader, CordovaRegistryPluginInfo plugin ) throws IOException {
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
				if("versions".equals(name) && plugin instanceof CordovaRegistryPlugin) { 
					parseDetailedVersions(reader, (CordovaRegistryPlugin)plugin);       
					break;
				}
				if("dist".equals(name) && plugin instanceof CordovaRegistryPluginVersion ){
					parseDistDetails(reader, (CordovaRegistryPluginVersion) plugin);
					break;
				}
				if("license".equals(name) && plugin instanceof CordovaRegistryPluginVersion ){
					CordovaRegistryPluginVersion v = (CordovaRegistryPluginVersion) plugin;
					v.setLicense(reader.nextString());
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

	private void parseDistDetails(JsonReader reader, CordovaRegistryPluginVersion plugin) throws IOException{
		reader.beginObject();
		JsonToken token = reader.peek();
		while(token != JsonToken.END_OBJECT){
			switch (token) {
			case NAME:
				String name = reader.nextName();
				if("shasum".equals(name)){
					plugin.setDistributionSHASum(reader.nextString());
					break;
				}
				if("tarball".equals(name)){
					plugin.setDistributionTarball(reader.nextString());
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

	private void parseDetailedVersions(JsonReader reader,
			CordovaRegistryPlugin plugin) throws IOException{
		reader.beginObject();//versions
		JsonToken token = reader.peek();
		while( token != JsonToken.END_OBJECT ){
			switch (token) {
			case NAME:
				CordovaRegistryPluginVersion version = new CordovaRegistryPluginVersion();
				version.setVersionNumber(reader.nextName());
				readPluginInfo(reader, version);
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

	private void parseLatestVersion(JsonReader reader, CordovaRegistryPluginInfo plugin) throws IOException{
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

	private void parseMaintainers(JsonReader reader, CordovaRegistryPluginInfo plugin) throws IOException{
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

	private void parseKeywords(JsonReader reader, CordovaRegistryPluginInfo plugin) throws IOException{
		reader.beginArray();
		while(reader.hasNext()){
			plugin.addKeyword(reader.nextString());
		}
		reader.endArray();
	}

}
