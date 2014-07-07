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
package org.eclipse.thym.core.platform;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class AbstractNativeBinaryBuildDelegate {
	
	private IProject project;
	private File destinationDir;
	private boolean release;
	private File buildArtifact;

	public void init(IProject project,  File destination) {
		this.destinationDir = destination;
		this.project = project;
	}
	
	public abstract void buildNow(IProgressMonitor monitor) throws CoreException;

	public IProject getProject() {
		return project;
	}

	public File getDestination() {
		return destinationDir;
	}

	public boolean isRelease() {
		return release;
	}

	public void setRelease(boolean release) {
		this.release = release;
	}

	/**
	 * Returns the build artifact that was last build by calling
	 * {@link #buildNow(IProgressMonitor)} method. 
	 * Will return null if the build is not yet complete or 
	 * {@link #buildNow(IProgressMonitor)} is not called yet for this instance.
	 * @return
	 */
	public File getBuildArtifact() {
		return buildArtifact;
	}
	
	protected void setBuildArtifact(File artifact){
		this.buildArtifact = artifact;
	}

}
