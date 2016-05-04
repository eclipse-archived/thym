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
package org.eclipse.thym.internal.ui.importer;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
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

	// @Override
	// TODO Remove when fully migrated to Platform 4.6.M7
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
	public Set<File> findConfigurableLocations(File root, IProgressMonitor monitor) {
		Set<File> res = new HashSet<>();
		LinkedList<File> directoriesToProcess = new LinkedList<>();
		directoriesToProcess.addFirst(root);
		while (!directoriesToProcess.isEmpty()) {
			File current = directoriesToProcess.pop();
			boolean configExist = false;
	        for(IPath path: PlatformConstants.CONFIG_PATHS){
	            File config = new File(current, path.toString());
	            if(config.isFile()){
	                configExist = true;
	                break;
	            }
	        }
	        File wwwFolder = new File(current, PlatformConstants.DIR_WWW);
	        if (configExist && wwwFolder.isDirectory()) {
	        	res.add(current);
	        } else if (current.isDirectory()) {
	        	directoriesToProcess.addAll(Arrays.asList(current.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return pathname.isDirectory();
					}
				})));
	        }
		}
		return res;
	}

	// @Override
	// TODO Uncomment when fully migrated to Platform 4.6.M7
	public Set<IFolder> getFoldersToIgnore(IProject project, IProgressMonitor monitor) {
		return Collections.emptySet();
	}
	
	// @Override
	// TODO Remove when fully migrated to Platform 4.6.M7
	public Set<IFolder> getDirectoriesToIgnore(IProject project, IProgressMonitor monitor) {
		return getDirectoriesToIgnore(project, monitor);
	}
}
