/*******************************************************************************
 * Copyright (c) 2013, 2017 Red Hat, Inc. 
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
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
import org.eclipse.thym.core.internal.util.HttpUtil;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.core.plugin.registry.plugin.CordovaRegistryPlugin;
import org.eclipse.thym.core.plugin.registry.plugin.CordovaRegistryPluginVersion;
import org.eclipse.thym.core.plugin.registry.plugin.PluginVersionDeserializer;
import org.eclipse.thym.core.plugin.registry.repo.CordovaRegistry;
import org.eclipse.thym.core.plugin.registry.repo.CordovaRegistrySearchPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class CordovaPluginRegistryManager {

	private static final String REGISTRY_URL = "http://registry.npmjs.org/";
	private static final String SEARCH_URL = "http://npmsearch.com/";
	private static final String PLUGIN_LIST_URL = "query?fields=name,keywords,description,author&q=keywords:%22ecosystem:cordova%22&sort=rating:desc&size=";

	private final File cacheHome;
	private HashMap<String, CordovaRegistryPlugin> detailedPluginInfoCache = new HashMap<String, CordovaRegistryPlugin>();

	public CordovaPluginRegistryManager() {
		cacheHome = new File(FileUtils.getUserDirectory(), ".plugman" + File.separator + "cache");
	}

	public CordovaRegistryPlugin getCordovaPluginInfo(String name) throws CoreException {

		CordovaRegistryPlugin plugin = detailedPluginInfoCache.get(name);
		if (plugin != null) {
			return plugin;
		}
		JsonReader reader = null;
		try {
			InputStream stream = HttpUtil.getHttpStream(REGISTRY_URL + name);
			reader = new JsonReader(new InputStreamReader(stream));
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(new TypeToken<List<CordovaRegistryPluginVersion>>(){}.getType(), new PluginVersionDeserializer());
			Gson gson = gsonBuilder.create();
			plugin = gson.fromJson(reader, CordovaRegistryPlugin.class);
			this.detailedPluginInfoCache.put(name, plugin);
			return plugin;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID,
					"Can not retrieve plugin information for " + name, e));
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					HybridCore.log(Status.ERROR, "Error occured when closing json reader", e);
				}
			}
		}
	}

	/**
	 * Returns a directory where the given version of the Cordova Plugin can be
	 * installed from. This method downloads the given cordova plugin if necessary.
	 * 
	 * @param plugin
	 * @return
	 */
	public File getInstallationDirectory(CordovaRegistryPlugin plugin, IProgressMonitor monitor) {
		if (monitor == null)
			monitor = new NullProgressMonitor();

		File pluginDir = getFromCache(plugin);
		if (pluginDir != null) {
			return pluginDir;
		}
		File newCacheDir = calculateCacheDir(plugin);

		IRetrieveFileTransfer transfer = HybridCore.getDefault().getFileTransferService();
		IFileID remoteFileID;

		try {
			remoteFileID = FileIDFactory.getDefault().createFileID(transfer.getRetrieveNamespace(),
					plugin.getVersions().get(0).getDist().getTarball());
			Object lock = new Object();
			PluginReceiver receiver = new PluginReceiver(newCacheDir, monitor, lock);
			synchronized (lock) {
				transfer.sendRetrieveRequest(remoteFileID, receiver, null);
				lock.wait();
			}
		} catch (FileCreateException | IncomingFileTransferException | InterruptedException e) {
			HybridCore.log(IStatus.ERROR, "Cordova plugin fetch error", e);
		}
		return new File(newCacheDir, "package");
	}

	private File getFromCache(CordovaRegistryPlugin plugin) {
		File cachedPluginDir = calculateCacheDir(plugin);
		File packageDir = new File(cachedPluginDir, "package");
		if (!packageDir.isDirectory()) {
			return null;
		}
		File pluginxml = new File(packageDir, PlatformConstants.FILE_XML_PLUGIN);
		if (cachedPluginDir.isDirectory() && pluginxml.exists())
			return packageDir;
		return null;
	}

	private File calculateCacheDir(CordovaRegistryPlugin plugin) {
		File cachedPluginDir = new File(this.cacheHome, plugin.getName() + File.separator + plugin.getVersions().get(0));

		return cachedPluginDir;
	}

	public List<CordovaRegistrySearchPlugin> retrievePluginInfos(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask("Retrieve plug-in registry catalog", 10);
		JsonReader reader = null;
		try {
			if (monitor.isCanceled()) {
				return null;
			}
			//find out how many plugins there are
			CordovaRegistry registry = getRegistry(1);
			monitor.worked(1);
			//get all plugins
			registry = getRegistry(registry.getTotal());
			monitor.worked(7);
			return registry.getCordovaPlugins();

		} catch (IOException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Can not retrieve plugin catalog", e));
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					HybridCore.log(Status.ERROR, "Error occured when closing json reader", e);
				}
			}
			monitor.done();
		}
	}
	
	private CordovaRegistry getRegistry(int size) throws IOException {
		InputStream stream = HttpUtil.getHttpStream(SEARCH_URL + PLUGIN_LIST_URL+size);
		JsonReader reader = new JsonReader(new InputStreamReader(stream));
		CordovaRegistry registry = new Gson().fromJson(reader, CordovaRegistry.class);
		return registry;
	}

}
