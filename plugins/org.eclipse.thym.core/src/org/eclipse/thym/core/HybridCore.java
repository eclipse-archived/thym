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
package org.eclipse.thym.core;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransferFactory;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.core.engine.AbstractEngineRepoProvider;
import org.eclipse.thym.core.engine.HybridMobileEngineLocator;
import org.eclipse.thym.core.engine.internal.cordova.CordovaEngineProvider;
import org.eclipse.thym.core.extensions.CordovaEngineRepoProvider;
import org.eclipse.thym.core.extensions.ExtensionPointProxy;
import org.eclipse.thym.core.extensions.NativeProjectBuilder;
import org.eclipse.thym.core.extensions.PlatformSupport;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

final public class HybridCore implements BundleActivator, DebugOptionsListener {

	/**
	 * Plugin ID
	 */	
	public static final String PLUGIN_ID = "org.eclipse.thym.core";
	
	private static BundleContext context;
	public static boolean DEBUG;
	private static DebugTrace TRACE;
	private static ILog logger;
	private ServiceTracker<IRetrieveFileTransferFactory, IRetrieveFileTransferFactory> retrievalFactoryTracker;
	private static HybridCore inst;

	public HybridCore(){
		super();
		inst =this;
	}
	
	
	public static HybridCore getDefault(){
		return inst;
	}
	
	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		HybridCore.context = bundleContext;
		logger = Platform.getLog(getContext().getBundle());
		Hashtable<String,Object> props = new Hashtable<String, Object>();
		props.put(org.eclipse.osgi.service.debug.DebugOptions.LISTENER_SYMBOLICNAME, PLUGIN_ID);
		context.registerService(DebugOptionsListener.class.getName(), this, props);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if(retrievalFactoryTracker != null ){
			retrievalFactoryTracker.close();
		}
		WidgetModel.shutdown();
		HybridCore.context = null;
	}
	
	/**
	 * Get an ECF based file transfer service.
	 * 
	 * @return retrieve file transfer
	 */
	public IRetrieveFileTransfer getFileTransferService(){
		IRetrieveFileTransferFactory factory =  getFileTransferServiceTracker().getService();
		return factory.newInstance();
	}
	
	
	/**
	 * Get the proxyservice
	 * @return
	 */
	public IProxyService getProxyService(){
		ServiceReference<IProxyService> sr = context.getServiceReference(IProxyService.class);
		return context.getService(sr);
	}
	
	private synchronized ServiceTracker<IRetrieveFileTransferFactory, IRetrieveFileTransferFactory> getFileTransferServiceTracker() {
		if (retrievalFactoryTracker == null) {
			retrievalFactoryTracker = new ServiceTracker<IRetrieveFileTransferFactory, IRetrieveFileTransferFactory>(getContext(), IRetrieveFileTransferFactory.class, null);
			retrievalFactoryTracker.open();
			startBundle("org.eclipse.ecf"); //$NON-NLS-1$
			startBundle("org.eclipse.ecf.provider.filetransfer"); //$NON-NLS-1$
		}
		return retrievalFactoryTracker;
	}
	
	private boolean startBundle(String bundleId) {
		ServiceTracker<PackageAdmin, PackageAdmin> packageAdminTracker = new ServiceTracker<PackageAdmin, PackageAdmin>(getContext(), PackageAdmin.class.getName(),null);
		packageAdminTracker.open();
		PackageAdmin packageAdmin = packageAdminTracker.getService();
		
		if (packageAdmin == null)
			return false;

		Bundle[] bundles = packageAdmin.getBundles(bundleId, null);
		if (bundles != null && bundles.length > 0) {
			for (int i = 0; i < bundles.length; i++) {
				try {
					if ((bundles[i].getState() & Bundle.INSTALLED) == 0) {
						bundles[i].start(Bundle.START_ACTIVATION_POLICY);
						bundles[i].start(Bundle.START_TRANSIENT);
						return true;
					}
				} catch (BundleException e) {
					// failed, try next bundle
				}
			}
		}
		return false;
	}
	
	public static List<HybridProject> getHybridProjects(){
		ArrayList<HybridProject> hybrids = new ArrayList<HybridProject>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : projects) {
			HybridProject hybridProject = HybridProject.getHybridProject(project);
			if(hybridProject != null ){
				hybrids.add(hybridProject);
			}
		
		}
		return hybrids;
	}

	@Override
	public void optionsChanged(DebugOptions options) {
		if(TRACE==null)
			TRACE = options.newDebugTrace(PLUGIN_ID);
		DEBUG = options.getBooleanOption(PLUGIN_ID+"/debug", true);	
	}
	
	public static void trace( String message){
		if( !DEBUG ) return;
		TRACE.trace(null, message);
	}
	
	public static void log(int status, String message, Throwable throwable ){
		logger.log(new Status(status, PLUGIN_ID,message,throwable));
	}

	/**
	 * Get the {@link PlatformSupport} objects defined by the extensions. 
	 * Returns all the defined PlatformSupports, it is up to the clients 
	 * to filter enabled/disabled PlatformSupports @see {@link PlatformSupport#isEnabled(org.eclipse.core.expressions.IEvaluationContext)}.
	 * 	
	 * @return list of project generators if any
	 */
	public static List<PlatformSupport> getPlatformSupports(){
		return ExtensionPointProxy.getNativeExtensionPointProxy(PlatformSupport.EXTENSION_POINT_ID, PlatformSupport.class);
	}
	
	/**
	 * Returns the {@link PlatformSupport} for the given platformID or null if none is present.
	 * 
	 * @param platformID
	 * @return PlatformSupport or null if no generator for the platformID is present
	 */
	public static PlatformSupport getPlatformSupport(String platformID){
		List<PlatformSupport> generators = getPlatformSupports();
		for (PlatformSupport projectGenerator : generators) {
			if(projectGenerator.getPlatformId().equals(platformID)){
				return projectGenerator;
			}
		}
		return null;
	}
	
	/**
	 * Returns the {@link NativeProjectBuilder} proxy objects defined by the 
	 * extensions. 
	 * 
	 * @return project builder extension points if any
	 */
	public static List<NativeProjectBuilder> getNativeProjectBuilders(){
		return ExtensionPointProxy.getNativeExtensionPointProxy(NativeProjectBuilder.EXTENSION_POINT_ID, NativeProjectBuilder.class);
	}
	
	/**
	 * Returns the {@link AbstractEngineRepoProvider} proxy objects defined by the 
	 * extensions. 
	 * 
	 * @return project builder extension points if any
	 */
	public static List<CordovaEngineRepoProvider> getCordovaEngineRepoProviders(){
		return ExtensionPointProxy.getNativeExtensionPointProxy(CordovaEngineRepoProvider.EXTENSION_POINT_ID, CordovaEngineRepoProvider.class);
	}
	
	/**
	 * Returns the {@link HybridMobileEngineLocator} implementations
	 * @return engine locators
	 */
	public static List<HybridMobileEngineLocator> getEngineLocators(){
		ArrayList<HybridMobileEngineLocator> locators = new ArrayList<HybridMobileEngineLocator>();
		locators.add(new CordovaEngineProvider());
		return locators;
	}
	
	/**
	 * Returns the preference value for custom engine locations.
	 * @return
	 */
	public String[] getCustomLibraryLocations(){
		String s =  Platform.getPreferencesService().getString("org.eclipse.thym.ui", PlatformConstants.PREF_CUSTOM_LIB_LOCS, null, null);
		if(s == null || s.length()<1){
			return new String[0];
		}
		return s.split(",");
	}

}
