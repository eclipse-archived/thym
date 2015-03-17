/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 	- Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.thym.ui.importer;

import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.ui.wizard.project.HybridProjectCreator;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;

public class CordovaProjectConfigurator implements ProjectConfigurator {


	@Override
	public boolean shouldBeAnEclipseProject(IContainer container, IProgressMonitor monitor) {
		// TODO share code with CanConvertToHybridTester
        boolean configExist = false;
        for(IPath path: PlatformConstants.CONFIG_PATHS){
            IFile config = container.getFile(path);
            if(config.exists()){
                configExist = true;
                break;
            }
        }
        IFolder wwwFile = container.getFolder(new Path(PlatformConstants.DIR_WWW));
        return configExist && wwwFile.exists();
	}

	@Override
	public boolean canConfigure(IProject project, Set<IPath> ignoredDirectories, IProgressMonitor monitor) {
		return shouldBeAnEclipseProject(project, monitor);
	}

	@Override
	public IWizard getConfigurationWizard() {
		return null;
	}

	@Override
	public void configure(IProject project, Set<IPath> ignoredDirectories, IProgressMonitor monitor) {
		try {
			new HybridProjectCreator().convertProject(project, monitor);
		} catch (CoreException ex) {
			ThymImporterActivator.getDefault().getLog().log(new Status(
					IStatus.ERROR,
					ThymImporterActivator.getDefault().getBundle().getSymbolicName(),
					ex.getMessage(),
					ex));
		}
	}

	@Override
	public Set<IFolder> getDirectoriesToIgnore(IProject project, IProgressMonitor monitor) {
		return null;
	}
}
