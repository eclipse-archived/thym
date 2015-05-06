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
package org.eclipse.thym.ui.plugins.internal;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.plugin.CordovaPluginManager;
import org.eclipse.thym.core.plugin.FileOverwriteCallback;
import org.eclipse.thym.core.plugin.RestorableCordovaPlugin;
import org.eclipse.thym.core.plugin.registry.CordovaPluginRegistryManager;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPlugin;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPlugin.RegistryPluginVersion;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class PluginRestoreOperation extends WorkspaceModifyOperation {
	
	private final RestorableCordovaPlugin[] restorables;
	private final HybridProject project;
	
	
	public PluginRestoreOperation(HybridProject project, RestorableCordovaPlugin[] restorables) {
		this.restorables = restorables;
		this.project = project;
	}

	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
			InvocationTargetException, InterruptedException {
		if(restorables == null || restorables.length < 1){
			HybridUI.log(IStatus.WARNING, "The restorables list is null or empty, aborting restore operation", null);
			return;
		}
		CordovaPluginManager pman = project.getPluginManager();
		FileOverwriteCallback cb = new FileOverwriteCallback() {
			@Override
			public boolean isOverwiteAllowed(String[] files) {
				return true ;
			}
		};
		for (RestorableCordovaPlugin feature : restorables) {
			switch (feature.getType()) {
			case REGISTRY:
				RegistryPluginVersion version = getVersion(feature);
				if(version == null ){
					throw new CoreException(new Status(IStatus.ERROR, HybridUI.PLUGIN_ID, 
							NLS.bind("Version {0} for Cordova plugin {1} does not exist on registry", new String[]{feature.getVersion(), })));
				}
				pman.installPlugin(version, cb, false, monitor);
				break;
			case GIT:
				try {
					pman.installPlugin(new URI(feature.getUrl()), cb, false, monitor);
				} catch (URISyntaxException e) {
					throw new CoreException(new Status(IStatus.ERROR, HybridUI.PLUGIN_ID, 
							NLS.bind("{0} is not a valid URI to restore Cordova plug-ins from Git",feature.getUrl()),e));
				}
				break;
			case LOCAL:
				pman.installPlugin(new File(feature.getPath()), cb, monitor);
				break;
			default:
				Assert.isTrue(false, "Unknown plugin restore type");
			}
			
		}
	}
	
	
	private RegistryPluginVersion getVersion(RestorableCordovaPlugin restorable) throws CoreException{
		CordovaPluginRegistryManager regMng = new CordovaPluginRegistryManager();
			CordovaRegistryPlugin plugin = regMng.getCordovaPluginInfo(restorable.getId());
			String version = restorable.getVersion();
			if(version == null){
				version = plugin.getLatestVersion();
			}
			return plugin.getVersion(version);
	}
	

}
