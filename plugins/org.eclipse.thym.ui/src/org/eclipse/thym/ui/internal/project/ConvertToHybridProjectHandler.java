/*******************************************************************************
 * Copyright (c) 2013, 2017 Red Hat, Inc. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat Inc. - initial API and implementation and/or initial
 * documentation
 *******************************************************************************/
package org.eclipse.thym.ui.internal.project;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.ui.wizard.project.HybridProjectCreator;

public class ConvertToHybridProjectHandler extends AbstractCordovaHandler{

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        
        IProject project = getProject(event);
        if(project != null ){
            final IProject theProject = project;//to pass to Job
            WorkspaceJob job = new WorkspaceJob(NLS.bind("Convert {0} to Hybrid Mobile project", project.getName())) {
                
                @Override
                public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                    HybridProjectCreator creator = new HybridProjectCreator();
                    creator.convertProject(theProject, new NullProgressMonitor());
                    return Status.OK_STATUS;
                }
            };
            job.schedule();
        }
        return null;
    }
}
