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
package org.eclipse.thym.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.ui.internal.preferences.HybridToolsPreferences;
import org.eclipse.thym.ui.internal.project.RestoreProjectListener;
import org.eclipse.thym.ui.internal.status.HybridMobileStatusExtension;
import org.eclipse.thym.ui.requirement.PlatformRequirementsExtension;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class HybridUI extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = PlatformConstants.HYBRID_UI_PLUGIN_ID;
	private static ILog logger;
	
	// The shared instance
	private static HybridUI plugin;
	private final RestoreProjectListener projectRestoreListener;
	
	/**
	 * The constructor
	 */
	public HybridUI() {
		projectRestoreListener = new RestoreProjectListener();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		logger = Platform.getLog(this.getBundle());
		IPreferenceStore store = getPreferenceStore();
		HybridToolsPreferences.init(store);
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				HybridToolsPreferences.getPrefs().loadValues(event);
				
			}
		});
		HybridToolsPreferences.getPrefs().loadValues();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(projectRestoreListener, IResourceChangeEvent.POST_CHANGE);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if(projectRestoreListener != null){
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(projectRestoreListener);
		}
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static HybridUI getDefault() {
		return plugin;
	}
	
	public static void log(int status, String message, Throwable throwable ){
		logger.log(new Status(status, PLUGIN_ID, message, throwable));
	}
	
	/**
     * Returns an image descriptor for the icon referenced by the given path
     * and contributor plugin
     * 
     * @param plugin symbolic name
     * @param path the path of the icon 
     * @return image descriptor or null
     */
    public static ImageDescriptor getImageDescriptor(String name, String path) {
		Bundle bundle = Platform.getBundle(name);
		if (path != null) {
			URL iconURL = FileLocator.find(bundle , new Path(path), null);
			if (iconURL != null) {
				return ImageDescriptor.createFromURL(iconURL);
			}
		}
		return null;
    }
    
    public static List<PlatformImage> getPlatformImages(){
    	IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(PlatformImage.EXTENSION_POINT_ID);
    	List<PlatformImage> images = new ArrayList<PlatformImage>();
    	for (int i = 0; i < configurationElements.length; i++) {
			PlatformImage image = new PlatformImage(configurationElements[i]);
			images.add(image);
		}
    	return images;
    }
    
    public static List<HybridMobileStatusExtension> getHybridMobileStatusExtensions(){
    	IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(HybridMobileStatusExtension.EXTENSION_POINT_ID);
    	List<HybridMobileStatusExtension> handlers = new ArrayList<HybridMobileStatusExtension>();
    	for (int i = 0; i < configurationElements.length; i++) {
			HybridMobileStatusExtension ext = new HybridMobileStatusExtension(configurationElements[i]);
			handlers.add(ext);
		}
    	return handlers;
    }
    
    /**
     * Returns list of all platform requirement extensions
     * @return
     */
    public static List<PlatformRequirementsExtension> getPlatformRequirementExtensions(){
    	IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(PlatformRequirementsExtension.EXTENSION_POINT_ID);
    	List<PlatformRequirementsExtension> handlers = new ArrayList<>();
    	if(configurationElements != null){
    		for(IConfigurationElement element: configurationElements){
    			PlatformRequirementsExtension handler = new PlatformRequirementsExtension(element);
    			handlers.add(handler);
    		}
    	}
    	return handlers;
    }
    
    /**
     * Returns list of platform requirement extensions for given platform
     * @param platformID
     * @return
     */
    public static List<PlatformRequirementsExtension> getPlatformRequirementExtensions(String platformID){
    	List<PlatformRequirementsExtension> filteredExtensions = new ArrayList<>();
    	List<PlatformRequirementsExtension> extensions = getPlatformRequirementExtensions();
    	for(PlatformRequirementsExtension extension: extensions){
    		if(extension.getPlatformID() != null && extension.getPlatformID().equals(platformID)){
    			filteredExtensions.add(extension);
    		}
    	}
    	return filteredExtensions;
    }
    
}
