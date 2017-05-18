/*******************************************************************************
 * Copyright (c) 2013, 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.internal.project;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.thym.core.HybridProject;
/**
 * 
 * A listener that restores the newly imported Cordova projects.
 * Restoration involves calling <i>cordova prepare</i>. 
 * 
 * @author Gorkem
 *
 */
public class RestoreProjectListener implements IResourceChangeListener {
	
	private class RestoreProjectJob extends Job{
		private IProject project;
		public RestoreProjectJob(IProject project){
			super("Prepare cordova project job");
			this.project = project;
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			HybridProject hybridProject  = HybridProject.getHybridProject(project);
			if(hybridProject != null){
				try{
					//do not call prepare for project with no engines otherwise cordova will complain
					if(hybridProject.getEngineManager().hasActiveEngine()) {
						hybridProject.prepare(monitor);
					}
				}catch(CoreException e){
					return e.getStatus();
				}
			}
			return Status.OK_STATUS;
		}	
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta[] projectDeltas = event.getDelta().getAffectedChildren();
		for (IResourceDelta delta : projectDeltas) {
			if((delta.getResource().getType() == IResource.PROJECT) && 
				(delta.getKind() == IResourceDelta.CHANGED || delta.getKind() == IResourceDelta.ADDED) 
					&& (delta.getFlags() & IResourceDelta.OPEN) != 0){
				IProject project = delta.getResource().getProject();
				if(project.isOpen()){
					HybridProject hybridProject  = HybridProject.getHybridProject(project);
					if(hybridProject != null ) {
						RestoreProjectJob job = new RestoreProjectJob(project);
						ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(project);
						job.setRule(rule);
						job.schedule();
					}
				}
			}
		}
	}
}
