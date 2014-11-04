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

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ecf.filetransfer.IncomingFileTransferException;
import org.eclipse.ecf.filetransfer.identity.FileCreateException;
import org.eclipse.ecf.filetransfer.identity.FileIDFactory;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.AbstractEngineRepoProvider;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.HybridMobileEngineLocator;
import org.eclipse.thym.core.engine.HybridMobileEngineLocator.EngineSearchListener;
import org.eclipse.thym.core.engine.HybridMobileLibraryResolver;
import org.eclipse.thym.core.engine.PlatformLibrary;
import org.eclipse.thym.core.engine.internal.cordova.DownloadableCordovaEngine.LibraryDownloadInfo;
import org.eclipse.thym.core.extensions.CordovaEngineRepoProvider;
import org.eclipse.thym.core.extensions.PlatformSupport;

import com.github.zafarkhaja.semver.Version;

public class CordovaEngineProvider implements HybridMobileEngineLocator, EngineSearchListener {
	
	private static final Version MIN_VERSION = Version.forIntegers(3, 0, 0);
	/**
	 * Engine id for the engine provided by the Apache cordova project.
	 */
	public static final String CORDOVA_ENGINE_ID = "cordova";
	public static final String ENGINE_NAME = "Apache Cordova";
	
	public static final String CUSTOM_CORDOVA_ENGINE_ID = "custom_cordova";
	
	private static ArrayList<HybridMobileEngine> engineList;

	
	/**
	 * List of engines that are locally available. This is the list of engines that 
	 * can be used by the projects.
	 * 
	 * @return 
	 */
	public List<HybridMobileEngine> getAvailableEngines() {
		initEngineList();
		return engineList;
	}

	
	private void resetEngineList(){
		engineList = null;
	}
	
	private void initEngineList() {
		if(engineList != null ) 
			return;
		engineList = new ArrayList<HybridMobileEngine>();
		
		File libFolder = getLibFolder().toFile();
		if( !libFolder.isDirectory()){
			//engine folder does not exist
			return;
		}
		//search for engines on default location.
		searchForRuntimes(new Path(libFolder.toString()), this, new NullProgressMonitor());
		//Now the custom locations 
		String[] locs = HybridCore.getDefault().getCustomLibraryLocations();
		if(locs != null ){
			for (int i = 0; i < locs.length; i++) {
				searchForRuntimes(new Path(locs[i]), this,  new NullProgressMonitor());
			}
		}
	}
	
	/**
	 * User friendly name of the engine
	 * 
	 * @return
	 */
	public String getName(){
		return ENGINE_NAME;
	}

	public HybridMobileEngine getEngine(String id, String version){
		initEngineList();
		for (HybridMobileEngine engine : engineList) {
			if(engine.getVersion().equals(version) && engine.getId().equals(id)){
				return engine;
			}
		}
		return null;
	}
	
	/**
	 * Helper method for creating engines.. Clients should not 
	 * use this method but use {@link #getAvailableEngines()} or 
	 * {@link #getEngine(String)}. This method is left public 
	 * mainly to help with testing.
	 * 
	 * @param version
	 * @param platforms
	 * @return
	 */
	public HybridMobileEngine createEngine(String id, String version, PlatformLibrary... platforms ){
		HybridMobileEngine engine = new HybridMobileEngine();
		engine.setId(id);
		engine.setName(ENGINE_NAME);
		engine.setVersion(version);
		for (int i = 0; i < platforms.length; i++) {
			engine.addPlatformLib(platforms[i]);
		}
		return engine;
	}
	
	public static IPath getLibFolder(){
		IPath path = new Path(FileUtils.getUserDirectory().toString());
		path = path.append(".cordova").append("lib");
		return path;
	}
	
	public List<DownloadableCordovaEngine> getDownloadableVersions()
			throws CoreException {
		AbstractEngineRepoProvider defaultProvider = new DefaultEngineRepoProvider();
		List<DownloadableCordovaEngine> result = defaultProvider.getEngines();
		List<CordovaEngineRepoProvider> providerProxies = HybridCore
				.getCordovaEngineRepoProviders();
		for (CordovaEngineRepoProvider providerProxy : providerProxies) {
			AbstractEngineRepoProvider provider = providerProxy
					.createProvider();
			List<DownloadableCordovaEngine> engines = provider.getEngines();
			if (engines != null) {
				if (provider.shouldOverride()) {
					result = engines;
				} else {
					addEngines(engines, result);
				}
			}
		}
		return result;

	}
	
	private void addEngines(List<DownloadableCordovaEngine> newEngines,
			List<DownloadableCordovaEngine> allEngines) {
		for (DownloadableCordovaEngine newEngine : newEngines) {
			boolean merged = false;
			for (DownloadableCordovaEngine engine : allEngines) {
				if (engine.merge(newEngine)) {
					merged = true;
					break;
				}
			}
			if (!merged) {
				allEngines.add(newEngine);
			}
		}
	}


	private DownloadableCordovaEngine getDownloadableCordovaEngine(String version){
		try {
			List<DownloadableCordovaEngine> versions = getDownloadableVersions();

			for (DownloadableCordovaEngine downloadableCordovaEngine : versions) {
				if (version.equals(downloadableCordovaEngine.getVersion())) {
					return downloadableCordovaEngine;
				}
			}
		}catch(CoreException e){
			HybridCore.log(IStatus.ERROR, "Could not retrieve downloadable engine list", e);
		}
		return null;
	}
	public void downloadEngine(String version, IProgressMonitor monitor, String... platforms) {
		if(monitor == null ){
			monitor = new NullProgressMonitor();
		}
		
		DownloadableCordovaEngine theEngine = getDownloadableCordovaEngine(version);
		
		IRetrieveFileTransfer transfer = HybridCore.getDefault().getFileTransferService();
		IFileID remoteFileID;

		int platformSize = platforms.length;
		Object lock = new Object();
		int incompleteCount = platformSize;
		monitor.beginTask("Download Cordova Engine "+version, platformSize *100 +1);
		monitor.worked(1);
		for (int i = 0; i < platformSize; i++) {
			LibraryDownloadInfo downloadInfo = theEngine.getPlatformLibraryInfo(platforms[i]);
			Assert.isNotNull(downloadInfo); //platforms.json does not have info on platform
			try {
				URI uri = URI.create(downloadInfo.getDownloadURL());
				remoteFileID = FileIDFactory.getDefault().createFileID(transfer.getRetrieveNamespace(), uri);
				SubProgressMonitor sm = new SubProgressMonitor(monitor, 100);
				if(monitor.isCanceled()){
					return;
				}
				transfer.sendRetrieveRequest(remoteFileID, new EngineDownloadReceiver(downloadInfo.getVersion(), platforms[i], lock, sm), null);
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
			}
		}
		monitor.done();
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
		DownloadableCordovaEngine engine = getDownloadableCordovaEngine(version);
		if(engine == null ){
			return false;
		}
		LibraryDownloadInfo lib = engine.getPlatformLibraryInfo(platformId);
		return lib != null;
	}


	@Override
	public void searchForRuntimes(IPath path, EngineSearchListener listener,
			IProgressMonitor monitor) {
		if( path == null ) return;
		File root = path.toFile();
		if(!root.isDirectory()) return;
		searchDir(root, listener, monitor);	
	}
	
	private void searchDir(File dir, EngineSearchListener listener, IProgressMonitor monitor){
		if("bin".equals(dir.getName())){
			File createScript = new File(dir,"create");
			if(createScript.exists()){
				Path libraryRoot = new Path(dir.getParent());
				List<PlatformSupport> platforms = HybridCore.getPlatformSupports();
				for (PlatformSupport platformSupport : platforms) {
					try {
						HybridMobileLibraryResolver resolver = platformSupport.getLibraryResolver();
						resolver.init(libraryRoot);
						if(resolver.isLibraryConsistent().isOK()){
							PlatformLibrary lib = new PlatformLibrary(platformSupport.getPlatformId(),libraryRoot);
							listener.libraryFound(lib);
							return;
						}
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
				if(!monitor.isCanceled())
					searchDir(dirs[i], listener, monitor);
			}
		}	
	}

	@Override
	public void libraryFound(PlatformLibrary library) {
		String version = getEngineVersion(library);
		if(version == null ){
			return;
		}
		boolean isDefaultLoc = getLibFolder().isPrefixOf(library.getLocation());
		String id = isDefaultLoc ? CORDOVA_ENGINE_ID: CUSTOM_CORDOVA_ENGINE_ID;
		Version v = Version.valueOf(version);
		if(v.greaterThanOrEqualTo(MIN_VERSION)){//check the minimum supported version
			HybridMobileEngine engine = getEngine(id, version);
			if(engine == null ){
				engineList.add(createEngine(id, version, library));
			}else{
				engine.addPlatformLib(library);
			}
		}
	}

	private String getEngineVersion(PlatformLibrary library) {
		String libVersion = library.getPlatformLibraryResolver().detectVersion();
		try{
			List<DownloadableCordovaEngine> engines = getDownloadableVersions();
			for (DownloadableCordovaEngine cordovaEngine : engines) {
				LibraryDownloadInfo platformLibraryInfo = cordovaEngine.getPlatformLibraryInfo(library.getPlatformId());
				if(platformLibraryInfo != null && platformLibraryInfo.getVersion().equals(libVersion) ){
					return cordovaEngine.getVersion();
				}
			}
		}catch (CoreException e){
			HybridCore.log(IStatus.WARNING, "Could not read downloadable engines", e);
		}
		return libVersion;
	}


	public void deleteEngineLibraries(HybridMobileEngine selectedEngine) {
		if(selectedEngine.getId().equals(CORDOVA_ENGINE_ID)){
			// Do not delete custom engines we do not manage them
			List<PlatformLibrary> libs = selectedEngine.getPlatformLibs();
			if(libs.isEmpty()) return;
			for (PlatformLibrary library : libs) {
				IPath path = library.getLocation();
				FileUtils.deleteQuietly(path.toFile());
			}
		}
		resetEngineList();
	}
	
}
