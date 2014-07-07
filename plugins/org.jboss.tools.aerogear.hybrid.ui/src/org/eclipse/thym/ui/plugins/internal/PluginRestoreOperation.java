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
package org.eclipse.thym.ui.plugins.internal;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.plugin.CordovaPluginManager;
import org.eclipse.thym.core.plugin.FileOverwriteCallback;
import org.eclipse.thym.core.plugin.registry.CordovaPluginRegistryManager;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPluginVersion;

public class PluginRestoreOperation extends WorkspaceModifyOperation {
	
	private final CordovaRegistryPluginVersion[] restorables;
	private final HybridProject project;
	
	
	public PluginRestoreOperation(HybridProject project, CordovaRegistryPluginVersion[] restorables) {
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
		CordovaPluginRegistryManager regMgr = new CordovaPluginRegistryManager(CordovaPluginRegistryManager.DEFAULT_REGISTRY_URL);
		CordovaPluginManager pman = project.getPluginManager();
		for (CordovaRegistryPluginVersion feature : restorables) {
			File dir = regMgr.getInstallationDirectory(feature, monitor);
			pman.installPlugin(dir, new FileOverwriteCallback() {
				
				@Override
				public boolean isOverwiteAllowed(String[] files) {
					// TODO Auto-generated method stub
					return true;
				}
			}, monitor);
		}
	}

}
