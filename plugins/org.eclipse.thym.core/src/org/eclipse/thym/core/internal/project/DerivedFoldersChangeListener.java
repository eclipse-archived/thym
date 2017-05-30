/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.internal.project;

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
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.platform.PlatformConstants;

/**
 * Listens to changes in hybrid project and marks all subfolders 
 * of {@link PlatformConstants#DERIVED_FOLDERS} as derived
 * @author rawagner
 *
 */
public class DerivedFoldersChangeListener implements IResourceChangeListener{
	
	private class DerivedFoldersJob extends Job{
		
		private HybridProject project;
		
		public DerivedFoldersJob(HybridProject project){
			super("Update Cordova derived folders");
			this.project = project;
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			try {
				project.updateDerivedFolders(monitor);
			} catch (CoreException e) {
				return e.getStatus();
			}
			return Status.OK_STATUS;
		}	
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		for (IResourceDelta delta : event.getDelta().getAffectedChildren()) {
			//check if resource is hybrid project
			if(delta.getResource().getType() == IResource.PROJECT){
				HybridProject hybridProject = HybridProject.getHybridProject((IProject)delta.getResource());
				if(hybridProject != null){
					DerivedFoldersJob job = new DerivedFoldersJob(hybridProject);
					job.schedule();
				}
			}
		}
		
	}

}
