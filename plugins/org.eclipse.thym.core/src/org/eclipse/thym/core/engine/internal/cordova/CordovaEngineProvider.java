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
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ecf.filetransfer.IncomingFileTransferException;
import org.eclipse.ecf.filetransfer.identity.FileCreateException;
import org.eclipse.ecf.filetransfer.identity.FileIDFactory;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.AbstractEngineRepoProvider;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.HybridMobileEngineLocator;
import org.eclipse.thym.core.engine.HybridMobileEngineLocator.EngineSearchListener;
import org.eclipse.thym.core.engine.HybridMobileLibraryResolver;
import org.eclipse.thym.core.extensions.CordovaEngineRepoProvider;
import org.eclipse.thym.core.extensions.PlatformSupport;

public class CordovaEngineProvider implements HybridMobileEngineLocator, EngineSearchListener {
	
	/**
	 * Engine id for the engine provided by the Apache cordova project.
	 */
	public static final String CORDOVA_ENGINE_ID = "cordova";
	public static final String CUSTOM_CORDOVA_ENGINE_ID = "custom_cordova";
	
	private volatile static ArrayList<HybridMobileEngine> engineList;

	
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
		return "Apache Cordova";
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
	 * @param resolver
	 * @return
	 */
	public HybridMobileEngine createEngine(String id, String version, HybridMobileLibraryResolver resolver, IPath location){
		HybridMobileEngine engine = new HybridMobileEngine();
		engine.setId(id);
		engine.setName(NLS.bind("{0}-{1}", new String[]{CORDOVA_ENGINE_ID,id}));
		engine.setResolver(resolver);
		engine.setVersion(version);
		engine.setLocation(location);
		return engine;
	}
	
	public static IPath getLibFolder(){
		IPath path = new Path(FileUtils.getUserDirectory().toString());
		path = path.append(".cordova").append("lib");
		return path;
	}
	
	public List<DownloadableCordovaEngine> getDownloadableVersions()
			throws CoreException {
		AbstractEngineRepoProvider provider = new NpmBasedEngineRepoProvider();
		IProduct product = Platform.getProduct();
		if (product != null) {
			String productId = Platform.getProduct().getId();
			List<CordovaEngineRepoProvider> providerProxies = HybridCore
					.getCordovaEngineRepoProviders();
			for (CordovaEngineRepoProvider providerProxy : providerProxies) {
				if (productId.equals(providerProxy.getProductId())) {
					provider = providerProxy.createProvider();
				}
			}
		}
		return provider.getEngines();
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
		monitor.worked(1);
		for (int i = 0; i < platformSize; i++) {
			monitor.beginTask("Download Cordova Engine "+engines[i].getVersion(), platformSize *100 +1);
			try {
				URI uri = URI.create(engines[i].getDownloadURL());
				remoteFileID = FileIDFactory.getDefault().createFileID(transfer.getRetrieveNamespace(), uri);
				SubProgressMonitor sm = new SubProgressMonitor(monitor, 100);
				if(monitor.isCanceled()){
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
		List<DownloadableCordovaEngine> engines = null;
		try {
			engines = getDownloadableVersions();
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
							HybridMobileEngine engine = createEngine(platformSupport.getPlatformId(),resolver.detectVersion(), resolver,libraryRoot);
							listener.engineFound(engine);
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
	public void engineFound(HybridMobileEngine engine) {
		engineList.add(engine);
	}

	public void deleteEngineLibraries(HybridMobileEngine selectedEngine) {
		IPath path = selectedEngine.getLocation();
		FileUtils.deleteQuietly(path.toFile());
		resetEngineList();
	}
	
}
