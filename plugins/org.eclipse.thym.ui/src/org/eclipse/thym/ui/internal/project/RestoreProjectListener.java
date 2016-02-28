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
package org.eclipse.thym.ui.internal.project;

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
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.internal.cordova.CordovaCLI;
import org.eclipse.thym.core.internal.cordova.ErrorDetectingCLIResult;
import org.eclipse.thym.core.natures.HybridAppNature;
import org.eclipse.thym.ui.HybridUI;
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
		protected IStatus run(IProgressMonitor monitor) {
			HybridProject hybridProject  = HybridProject.getHybridProject(project);
			if(hybridProject == null ) return Status.OK_STATUS;
			IStatus status = Status.OK_STATUS;
			try{
				status = CordovaCLI.newCLIforProject(hybridProject)
						.prepare( monitor, "")
						.convertTo(ErrorDetectingCLIResult.class)
						.asStatus();
				hybridProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
			}catch(CoreException e){
				return e.getStatus();
			}
			return status;
		}	
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta[] projectDeltas = event.getDelta().getAffectedChildren();
		for (IResourceDelta delta : projectDeltas) {
			if (delta.getKind() == IResourceDelta.ADDED && (delta.getFlags() & IResourceDelta.OPEN) != 0
					&& delta.getResource().getType() == IResource.PROJECT) {
				IProject project = delta.getResource().getProject();
				try {
					if (project.hasNature(HybridAppNature.NATURE_ID)) {
						RestoreProjectJob job = new RestoreProjectJob(project);
						job.schedule();
					}
				} catch (CoreException e) {
					HybridUI.log(IStatus.ERROR, NLS.bind("Can not check nature for project {0} ", project.getName()),
							e);
				}
			}
		}
	}
}
