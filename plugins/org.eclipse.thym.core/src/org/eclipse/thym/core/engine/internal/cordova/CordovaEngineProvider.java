/*******************************************************************************
 * Copyright (c) 2013, 2016 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.engine.internal.cordova;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ecf.filetransfer.IncomingFileTransferException;
import org.eclipse.ecf.filetransfer.identity.FileCreateException;
import org.eclipse.ecf.filetransfer.identity.FileIDFactory;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.AbstractEngineRepoProvider;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.HybridMobileEngineLocator;
import org.eclipse.thym.core.engine.HybridMobileEngineManager;
import org.eclipse.thym.core.engine.HybridMobileEngineLocator.EngineSearchListener;
import org.eclipse.thym.core.engine.HybridMobileLibraryResolver;
import org.eclipse.thym.core.extensions.CordovaEngineRepoProvider;
import org.eclipse.thym.core.extensions.PlatformSupport;
import org.eclipse.thym.core.internal.util.EngineUtils;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.osgi.service.prefs.BackingStoreException;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class CordovaEngineProvider implements HybridMobileEngineLocator, EngineSearchListener {
	
	/**
	 * Engine id for the engine provided by the Apache cordova project.
	 */
	public static final String CORDOVA_ENGINE_ID = "cordova";
	public static final String CUSTOM_CORDOVA_ENGINE_ID = "custom_cordova";
	
	private volatile static Set<HybridMobileEngine> engineList;
	private static CordovaEngineProvider instance;
	
	private CordovaEngineProvider(){
	}
	
	public static CordovaEngineProvider getInstance(){
		if(instance == null){
			instance = new CordovaEngineProvider();
		}
		return instance;
	}
	
	/**
	 * Initialize engine list with latest stable version for all supported platforms
	 */
	private void initEngineList(){
		if(engineList == null ) {
			engineList = new LinkedHashSet<HybridMobileEngine>();
			List<DownloadableCordovaEngine> downloadableEngines = null;
			List<PlatformSupport> platforms = HybridCore.getPlatformSupports();
			for(PlatformSupport support: platforms){
				try {
					downloadableEngines = getDownloadableEngines(support.getPlatformId());
				} catch (CoreException e) {
					HybridCore.log(IStatus.ERROR, "Error retrieving downloadable engines", e);
				}
				if(downloadableEngines != null){
					DownloadableCordovaEngine latestEngine = getLatestVersion(downloadableEngines);
					if(latestEngine != null){
						engineList.add(createEngine(latestEngine.getPlatformId(), latestEngine.getVersion()));
					}
				}
			}
			engineList.addAll(getPreferencesEngines());
		}
	}

	public Set<HybridMobileEngine> getAvailableEngines() {
		initEngineList();
		return engineList;
	}

	
	private void resetEngineList(){
		engineList = null;
	}
	
	/**
	 * User friendly name of the engine
	 * 
	 * @return
	 */
	public String getName(){
		return "Apache Cordova";
	}

	public HybridMobileEngine getEngine(String name, String spec){
		initEngineList();
		for (HybridMobileEngine engine : engineList) {
			if(EngineUtils.getExactVersion(engine.getSpec()).equals(EngineUtils.getExactVersion(spec)) && engine.getName().equals(name)){
				return engine;
			}
		}
		return null;
	}
	
	public static IPath getLibFolder(){
		IPath path = new Path(FileUtils.getUserDirectory().toString());
		path = path.append(".cordova").append("lib");
		return path;
	}
	
	public List<DownloadableCordovaEngine> getDownloadableEngines() throws CoreException {
		AbstractEngineRepoProvider provider = new NpmBasedEngineRepoProvider();
		IProduct product = Platform.getProduct();
		if (product != null) {
			String productId = Platform.getProduct().getId();
			List<CordovaEngineRepoProvider> providerProxies = HybridCore.getCordovaEngineRepoProviders();
			for (CordovaEngineRepoProvider providerProxy : providerProxies) {
				if (productId.equals(providerProxy.getProductId())) {
					provider = providerProxy.createProvider();
				}
			}
		}
		return provider.getEngines();
	}
	
	public List<DownloadableCordovaEngine> getDownloadableEngines(String platformId) throws CoreException {
		AbstractEngineRepoProvider provider = new NpmBasedEngineRepoProvider();
		IProduct product = Platform.getProduct();
		if (product != null) {
			String productId = Platform.getProduct().getId();
			List<CordovaEngineRepoProvider> providerProxies = HybridCore.getCordovaEngineRepoProviders();
			for (CordovaEngineRepoProvider providerProxy : providerProxies) {
				if (productId.equals(providerProxy.getProductId())) {
					provider = providerProxy.createProvider();
				}
			}
		}
		return provider.getEngines(platformId);
	}
	
	private DownloadableCordovaEngine getLatestVersion(List<DownloadableCordovaEngine> engines){
		if(engines == null || engines.isEmpty()){
			return null;
		}
		List<DownloadableCordovaEngine> stableEngines = new ArrayList<>();
		for(DownloadableCordovaEngine e: engines){
			if(!e.isNightlyBuild()){
				stableEngines.add(e);
			}
		}
		Collections.sort(stableEngines, new VersionComparator());
		return stableEngines.get(0);
	}
	
	public DownloadableCordovaEngine getLatestVersion(String platformId){
		List<DownloadableCordovaEngine> downloadableEngines = null;
		try {
			downloadableEngines = getDownloadableEngines(platformId);
		} catch (CoreException e) {
			HybridCore.log(Status.ERROR, "Error while retrieving downloadable engines", e);
		}
		if(downloadableEngines != null){
			return getLatestVersion(downloadableEngines);
		}
		return null;
	}
	
	private class VersionComparator implements Comparator<DownloadableCordovaEngine>{

		@Override
		public int compare(DownloadableCordovaEngine o1, DownloadableCordovaEngine o2) {
			Version v1 = Version.valueOf(o1.getVersion());
			Version v2 = Version.valueOf(o2.getVersion());
			return v2.compareTo(v1);
		}
		
	}


	public void downloadEngine(DownloadableCordovaEngine[] engines, IProgressMonitor monitor) {
		if(monitor == null ){
			monitor = new NullProgressMonitor();
		}
		
		IRetrieveFileTransfer transfer = HybridCore.getDefault().getFileTransferService();
		IFileID remoteFileID;

		int platformSize = engines.length;
		Object lock = new Object();
		int incompleteCount = platformSize;
		SubMonitor sm = SubMonitor.convert(monitor,platformSize );
		for (int i = 0; i < platformSize; i++) {
			sm.setTaskName("Download Cordova Engine "+engines[i].getVersion());
			try {
				URI uri = URI.create(engines[i].getDownloadURL());
				remoteFileID = FileIDFactory.getDefault().createFileID(transfer.getRetrieveNamespace(), uri);
				if(sm.isCanceled()){
					return;
				}
				transfer.sendRetrieveRequest(remoteFileID, new EngineDownloadReceiver(engines[i].getVersion(), engines[i].getPlatformId(), lock, sm), null);
			
			} catch (FileCreateException e) {
				HybridCore.log(IStatus.ERROR, "Engine download file create error", e);
			} catch (IncomingFileTransferException e) {
				HybridCore.log(IStatus.ERROR, "Engine download file transfer error", e);
			}
		}
		synchronized (lock) {
			while(incompleteCount >0){
				try {
					lock.wait();
				} catch (InterruptedException e) {
					HybridCore.log(IStatus.INFO, "interrupted while waiting for all engines to download", e);
				}
				incompleteCount--;
				sm.worked(1);
			}
		}
		resetEngineList();
	}
	
	/**
	 * Check if the platform is supported by this provider.
	 * 
	 * @param platformId
	 * @return
	 */
	public boolean isSupportedPlatform(String version, String platformId){
		Assert.isNotNull(platformId);
		Assert.isNotNull( version);
		List<DownloadableCordovaEngine> engines = null;
		try {
			engines = getDownloadableEngines();
		} catch (CoreException e) {
			HybridCore.log(IStatus.ERROR, "Error retrieving downloadable engines", e);
		}
		if(engines == null ){
			return false;
		}
		for (DownloadableCordovaEngine downloadable : engines) {
			if(downloadable.getVersion().equals(version) 
					&& downloadable.getPlatformId().equals(platformId) ){
				return true;
			}
		}
		return false;
	}


	@Override
	public void searchForRuntimes(IPath path, EngineSearchListener listener, IProgressMonitor monitor) {
		if(path != null){
			File root = path.toFile();
			if(root.isDirectory()){
				searchDir(root, listener, monitor);	
			}
		}
	}
	
	private void searchDir(File dir, EngineSearchListener listener, IProgressMonitor monitor){
		if(monitor.isCanceled()){
			return;
		}
		if("bin".equals(dir.getName())){
			File createScript = new File(dir,"create");
			if(createScript.exists()){
				File packageJson = new File(dir.getParent()+"/package.json");
				if(packageJson.exists()){
					JsonParser parser = new JsonParser();
					JsonReader reader = null;
					try {
						reader = new JsonReader(new FileReader(packageJson));
					} catch (FileNotFoundException e) {
						HybridCore.log(IStatus.ERROR, "package.json was not found", e);
					}
					JsonObject root = parser.parse(reader).getAsJsonObject();
					String name = root.get("name").getAsString();
					if(name.startsWith("cordova-")){
						name=name.replace("cordova-", "");
					}
					PlatformSupport support = HybridCore.getPlatformSupport(name);
					try {
						HybridMobileEngine engine = 
								createEngine(name, dir.getParent(), support.getLibraryResolver());
						listener.engineFound(engine);
						return;
					} catch (CoreException e) {
						HybridCore.log(IStatus.WARNING, "Error on engine search", e);
					}
				}
			}
		}
		//search the sub-directories
		File[] dirs = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}
		});
		if(dirs != null ){
			for (int i = 0; i < dirs.length; i++) {
				if(!monitor.isCanceled()){
					String name = dirs[i].getName();
					if(name.equals("npm_cache") || name.equals("tmp")){
						continue;
					}
					searchDir(dirs[i], listener, monitor);
				}
			}
		}	
	}

	@Override
	public void engineFound(HybridMobileEngine engine) {
		initEngineList();
		if(engineList.add(engine)){
			IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(HybridCore.PLUGIN_ID);
			preferences.put(engine.getName(), engine.getSpec());
		}
	}
	
	/**
	 * Returns the {@link HybridMobileEngine}s specified within Thym preferences.
	 *
	 * </p>
	 * If no engines have been added, returns an empty array. Otherwise returns
	 * either the user's preference, or, by default, the most recent version
	 * available for each platform.
	 *
	 * @see HybridMobileEngineManager#getActiveEngines()
	 * @return possibly empty array of {@link HybridMobileEngine}s
	 */
	public List<HybridMobileEngine> defaultEngines() {
		List<HybridMobileEngine> defaultEngines = new ArrayList<>();
		List<PlatformSupport> platforms = HybridCore.getPlatformSupports();
		for(PlatformSupport support: platforms){
			DownloadableCordovaEngine latestDownloadable = getLatestVersion(support.getPlatformId());
			if(latestDownloadable != null){
				HybridMobileEngine engine = createEngine(latestDownloadable.getPlatformId(), latestDownloadable.getVersion());
				defaultEngines.add(engine);
			}
		}
		
		List<HybridMobileEngine> userEngines = getUserDefaultEngines();
		for(HybridMobileEngine userEngine: userEngines){
			//user preferences overrides latest engine
			int engineFoundIndex = -1;
			for(HybridMobileEngine e: defaultEngines){
				if(e.getName().equals(userEngine.getName())){
					engineFoundIndex = defaultEngines.indexOf(e);
					break;
				}
			}
			if(engineFoundIndex != -1){
				defaultEngines.remove(engineFoundIndex);
			}
			defaultEngines.add(userEngine);
		}
		return defaultEngines;
	}
	
	private List<HybridMobileEngine> getUserDefaultEngines(){
		List<HybridMobileEngine> userEngines = new ArrayList<>();
		String pref =  Platform.getPreferencesService().getString(PlatformConstants.HYBRID_UI_PLUGIN_ID, PlatformConstants.PREF_DEFAULT_ENGINE, null, null);
		if(pref != null && !pref.isEmpty()){
			String[] engineStrings = pref.split(",");
			for (String engineString : engineStrings) {
				String[] engineInfo = engineString.split(":");
				HybridMobileEngine engine = createEngine(engineInfo[0], engineInfo[1]);
				userEngines.add(engine);
			}
		}
		return userEngines;
	}
	
	private List<HybridMobileEngine> getPreferencesEngines(){
		List<HybridMobileEngine> preferencesEngines = new ArrayList<>();
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(HybridCore.PLUGIN_ID);
		try {
			for(String key: preferences.keys()){
				String value = preferences.get(key, "");
				preferencesEngines.add(createEngine(key, value));
				
			}
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return preferencesEngines;
	}
	
	public HybridMobileEngine createEngine(String name, String spec){
		HybridMobileEngine existingEngine = getEngine(name, spec);
		if(existingEngine != null){
			return existingEngine;
		}
		PlatformSupport support = HybridCore.getPlatformSupport(name);
		if(support == null){
			return new HybridMobileEngine(name, spec, null);
		}
		try {
			return new HybridMobileEngine(name, spec, support.getLibraryResolver());
		} catch (CoreException e) {
			HybridCore.log(Status.ERROR, "Error occured when getting library resolver", e);
		}
		return null;
	}
	
	public HybridMobileEngine createEngine(String name, String spec, HybridMobileLibraryResolver resolver){
		HybridMobileEngine existingEngine = getEngine(name, spec);
		if(existingEngine != null){
			return existingEngine;
		}
		return new HybridMobileEngine(name, spec, resolver);
	}
	
	public void deleteEngine(HybridMobileEngine engine){
		engineList.remove(engine);
	}
	
}
