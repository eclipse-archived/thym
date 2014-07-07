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

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.natures.HybridAppNature;
import org.eclipse.thym.core.plugin.RestorableCordovaPlugin;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class RestorePluginsListener implements IResourceChangeListener {
	
	private class PluginCheckJob extends Job{
		private IProject project;
		public PluginCheckJob(IProject project){
			super("Saved Cordova plug-ins job");
			this.project = project;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			HybridProject hybridProject  = HybridProject.getHybridProject(project);
			if(hybridProject == null ) return Status.OK_STATUS;
			try{
				List<RestorableCordovaPlugin> restorables = hybridProject.getPluginManager().getRestorablePlugins(monitor);
				if(!restorables.isEmpty()){
					runRestoreUI(hybridProject);
				}
			}catch(CoreException e){
				HybridUI.log(IStatus.WARNING, "Could not restore Cordova plug-ins from config.xml",e);
			}
			return Status.OK_STATUS;
		}
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta[] projectDeltas = event.getDelta().getAffectedChildren();
			for (IResourceDelta delta : projectDeltas) {
				if(delta.getKind() == IResourceDelta.ADDED 
						&& (delta.getFlags() & IResourceDelta.OPEN) != 0 
						&& delta.getResource().getType() == IResource.PROJECT
						){
					IProject project = delta.getResource().getProject();
					try {
						if(project.hasNature(HybridAppNature.NATURE_ID)){
							PluginCheckJob job = new PluginCheckJob(project);
							job.schedule();
						}
					} catch (CoreException e) {
						HybridUI.log(IStatus.ERROR,  NLS.bind("Can not check nature for project {0} ",project.getName()), e);
					}
				}
			}
	}
	
	private void runRestoreUI(final HybridProject project) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				Wizard wizard = new RestorePluginWizard(project);
				WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
				dialog.setMinimumPageSize(550, 450);//TODO: needs a more clever way to set this values
				dialog.open();
			}
		});
	}

	
}
