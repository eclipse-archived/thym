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
package org.eclipse.thym.core.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.platform.IPluginInstallationAction;

public class PluginInstallActionsRunOperation implements IWorkspaceRunnable {
	
	private final List<IPluginInstallationAction> actions;
	private final boolean runUnInstall;
	private final FileOverwriteCallback overwrite;
	private final IProject project;
	
	
	public PluginInstallActionsRunOperation(final List<IPluginInstallationAction> actions, boolean runUnInstall, FileOverwriteCallback overwrite, IProject project){
		this.actions = actions;
		this.overwrite = overwrite;
		this.runUnInstall = runUnInstall;
		this.project = project;
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		if (overwrite != null) {
			ArrayList<String> list = new ArrayList<String>();
			for (IPluginInstallationAction action : actions) {
				String[] files = action.filesToOverwrite();
				if (files != null && files.length > 0) {
					list.addAll(Arrays.asList(files));
				}
			}
			if(!list.isEmpty() && overwrite.isOverwiteAllowed(list.toArray(new String[list.size()])) == false ){
				HybridCore.log(IStatus.INFO, "File overwrite not allowed cancelled Cordova plugin installation", null);
				return;
			}
		}
		
		Stack<IPluginInstallationAction> executed = new Stack<IPluginInstallationAction>();
		boolean rollback = false;
		try {
			for (IPluginInstallationAction action : actions) {
				if (monitor.isCanceled()) {
					rollback = true;
					break;
				}
				if (runUnInstall) {
					action.unInstall();
				} else {
					action.install();
				}
				monitor.worked(1);
				executed.push(action);
			}
		} catch (CoreException e) {
			HybridCore.log(IStatus.ERROR, "Error while installing plugin", e);
			rollback = true;
		}
		if (rollback) {
			while (!executed.empty()) {
				IPluginInstallationAction action = executed.pop();
				try {
					if(runUnInstall){
						action.install();
					}else{
						action.unInstall();
					}
				} catch (CoreException e) {
					HybridCore.log(IStatus.ERROR,
							"Error rolling back install action", e);
				}
			}
		}
		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

}
