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
package org.eclipse.thym.android.core;

import java.io.File;
import java.util.Hashtable;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class AndroidCore implements BundleActivator, DebugOptionsListener {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.thym.android.core"; //$NON-NLS-1$

	// The shared instance
	private static BundleContext context;
	private static ILog logger;
	public static boolean DEBUG;
	private static DebugTrace TRACE;

	public static BundleContext getContext() {
		return context;
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		AndroidCore.context = context;
		logger = Platform.getLog(getContext().getBundle());
		Hashtable<String,Object> props = new Hashtable<String, Object>();
		props.put(org.eclipse.osgi.service.debug.DebugOptions.LISTENER_SYMBOLICNAME, PLUGIN_ID);
		context.registerService(DebugOptionsListener.class.getName(), this, props);


	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		AndroidCore.context = null;
	}
	
	/**
	 * Returns location of Android SDK defined in preferences. If SDK is not defined in preferences returns 
	 * location from environment variable ANDROID_HOME. If location is not valid or is not defined, null is returned.
	 * @return location of Android SDK or null if location is not valid
	 */
	public static String getSDKLocation(){
		String prefSDKLocation =Platform.getPreferencesService().getString("org.eclipse.thym.ui", AndroidConstants.PREF_ANDROID_SDK_LOCATION, null, null);
		if(isValidSDKLocation(prefSDKLocation)){
			return prefSDKLocation;
		}
		String envSDKLocation = System.getenv(AndroidConstants.ANDROID_HOME);
		if(isValidSDKLocation(envSDKLocation)){
			return envSDKLocation;
		}
		return null;
	}
	
	private static boolean isValidSDKLocation(String sdkLocation){
		if(sdkLocation != null && !sdkLocation.isEmpty()){
			File file = new File(sdkLocation);
			if(!file.isDirectory()){
				return false;
			}
			File toolsFolder = new File(file,"tools");
			File platformToolsFolder = new File(file, "platform-tools");
			if(!toolsFolder.isDirectory() || !platformToolsFolder.isDirectory()){
				return false;
			}
			return true;
		}
		return false;
	}
	
	public static void log(int status, String message, Throwable throwable ){
		logger.log(new Status(status, PLUGIN_ID, message,throwable));
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
}
