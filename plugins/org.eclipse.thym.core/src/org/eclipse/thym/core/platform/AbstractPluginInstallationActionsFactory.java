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

public abstract class AbstractPluginInstallationActionsFactory {
	
	private File pluginDirectory;
	private File projectDirectory;
	private IProject project;
	
	public void init(File pluginHome, IProject  project, File platformProjectRoot) {
		this.projectDirectory = platformProjectRoot;
		this.pluginDirectory = pluginHome;
		this.project = project;
	}

	public abstract  IPluginInstallationAction getSourceFileAction(String src,
			String targetDir, String framework, String pluginId, String compilerFlags);

	public abstract IPluginInstallationAction getResourceFileAction(String src);
	 
	public abstract IPluginInstallationAction getHeaderFileAction(String src, String targetDir, String pluginId);

	public abstract IPluginInstallationAction getAssetAction(String src, String target);

	public abstract IPluginInstallationAction getConfigFileAction(String target, String parent, String value);

	public abstract IPluginInstallationAction getLibFileAction(String src, String arch);

	public abstract IPluginInstallationAction getFrameworkAction(String src, String weak, String pluginId, String custom, String type, String parent); 
	
	public abstract IPluginInstallationAction getJSModuleAction(String src, String pluginId, String jsModuleName);
	
	public abstract IPluginInstallationAction getCreatePluginJSAction(String content); 
	
	public File getPluginDirectory(){
		return pluginDirectory;
	}
	
	public File getProjectDirectory(){
		return projectDirectory;
	}
	
	public IProject getProject(){
		return project;
	}


}
