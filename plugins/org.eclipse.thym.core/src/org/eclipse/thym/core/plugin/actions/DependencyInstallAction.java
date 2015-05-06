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
package org.eclipse.thym.core.plugin.actions;

import java.net.URI;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.platform.IPluginInstallationAction;
import org.eclipse.thym.core.plugin.CordovaPluginManager;
import org.eclipse.thym.core.plugin.FileOverwriteCallback;
import org.eclipse.thym.core.plugin.registry.CordovaPluginRegistryManager;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPlugin;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPlugin.RegistryPluginVersion;

public class DependencyInstallAction implements IPluginInstallationAction {

	private final HybridProject project;
	private final String dependencyPluginId;
	private final URI uri;
	private final FileOverwriteCallback overwriteCallback;
	
	
	public DependencyInstallAction(String dependencyId, URI uri,
			 HybridProject project, FileOverwriteCallback overwrite) {
		this.project = project;
		this.uri = uri;
		this.dependencyPluginId = dependencyId;
		this.overwriteCallback = overwrite;
	}

	@Override
	public void install() throws CoreException {
		CordovaPluginManager pluginManager = project.getPluginManager();
		if(!pluginManager.isPluginInstalled(dependencyPluginId)){
			if( uri != null){
				pluginManager.installPlugin(uri,overwriteCallback, true, new NullProgressMonitor());
			}else{//install from registry
				CordovaPluginRegistryManager manager = new CordovaPluginRegistryManager();
				CordovaRegistryPlugin plugin = manager.getCordovaPluginInfo(dependencyPluginId);
				List<RegistryPluginVersion> versions = plugin.getVersions();
				for (RegistryPluginVersion version : versions) {
					if(plugin.getLatestVersion().equals(version.getVersionNumber())){
						pluginManager.installPlugin(version, this.overwriteCallback, true, new NullProgressMonitor());
					}
				}
			}
			
		}
	}

	@Override
	public void unInstall() throws CoreException {
		// Let user uninstall them manually.
//		CordovaPluginManager pluginManager = project.getPluginManager();
//		pluginManager.unInstallPlugin(dependencyPluginId, new NullProgressMonitor());
	}

	@Override
	public String[] filesToOverwrite() {
		// no need to report here.
		return new String[0];
	}

}
