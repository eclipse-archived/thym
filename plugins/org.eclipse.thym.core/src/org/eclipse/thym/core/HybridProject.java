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
package org.eclipse.thym.core;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.core.engine.HybridMobileEngineManager;
import org.eclipse.thym.core.internal.cordova.CordovaProjectCLI;
import org.eclipse.thym.core.internal.cordova.ErrorDetectingCLIResult;
import org.eclipse.thym.core.natures.HybridAppNature;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.core.plugin.CordovaPluginManager;

/**
 * Handle for the mobile hybrid project types 
 * @author Gorkem Ercan
 *
 */
public class HybridProject implements IAdaptable {
	
	private IProject kernelProject;
	private CordovaPluginManager pluginManager;
	private HybridMobileEngineManager engineManager;
	
	private HybridProject(IProject project) {
		this.kernelProject = project;
	} 
	
	/**
	 * Returns the underlying {@link IProject} instance.
	 * @return kernel project
	 */
	public IProject getProject(){
		return kernelProject;
	}
	
	/**
	 * Creates a hybrid project handle for the project. 
	 * Can return null if the given project is not a hybrid 
	 * mobile project.
	 * 
	 * @param project
	 * @return hybrid project or null
	 */
	public static HybridProject getHybridProject(IProject project) {
		if(project == null ) 
			return null;
		try {
			if (project.hasNature(HybridAppNature.NATURE_ID)) {
				return new HybridProject(project);
			}
		} catch (CoreException e) {
			// let it return null
		}
		return null;
	}
	/**
	 * Convenience method for getting a handle  to the hybrid project with 
	 * name projectName. 
	 * 
	 * @see #getHybridProject(IProject)
	 * @param projectName
	 * @return
	 */
	public static HybridProject getHybridProject(String projectName) {
		if(projectName == null || projectName.isEmpty())
			return null;
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if(project == null )
			return null; 
		return getHybridProject(project);
	}
	
	/**
	 * Retrieves the {@link CordovaPluginManager} instance for this project.
	 *  
	 * @return plugin manager
	 */
	public CordovaPluginManager getPluginManager(){
		if(pluginManager == null ){
			pluginManager = new CordovaPluginManager(this);
		}
		return pluginManager;
	}
	
	public void build(IProgressMonitor monitor, final String... buildOptions) throws CoreException{
		ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(getProject());
		try{
			Job.getJobManager().beginRule(rule, monitor);
		
			SubMonitor sm = SubMonitor.convert(monitor);
			sm.setWorkRemaining(100);
			ErrorDetectingCLIResult status = getProjectCLI().build(sm.newChild(70),buildOptions).convertTo(ErrorDetectingCLIResult.class);
			getProject().refreshLocal(IResource.DEPTH_INFINITE, sm.newChild(30));
			if(status.asStatus().getSeverity() == IStatus.ERROR){
				throw new CoreException(status.asStatus());
			}
		} finally {
			Job.getJobManager().endRule(rule);
		}
	}
	
	public void prepare(IProgressMonitor monitor, final String...options) throws CoreException{
		ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(getProject());
		try{
			Job.getJobManager().beginRule(rule, monitor);
		
			SubMonitor sm = SubMonitor.convert(monitor);
			sm.setWorkRemaining(100);
			ErrorDetectingCLIResult status = getProjectCLI().prepare(sm.newChild(70),options).convertTo(ErrorDetectingCLIResult.class);
			getProject().refreshLocal(IResource.DEPTH_INFINITE, sm.newChild(30));
			if(status.asStatus().getSeverity() == IStatus.ERROR){
				throw new CoreException(status.asStatus());
			}
		} finally {
			Job.getJobManager().endRule(rule);
		}
	}
	
	public void emulate(IProgressMonitor monitor, final String...options) throws CoreException{
		ErrorDetectingCLIResult status = getProjectCLI().emulate(monitor,options).convertTo(ErrorDetectingCLIResult.class);
		if(status.asStatus().getSeverity() == IStatus.ERROR){
			throw new CoreException(status.asStatus());
		}
	}
	
	public void run(IProgressMonitor monitor, final String...options) throws CoreException{
		ErrorDetectingCLIResult status = getProjectCLI().run(monitor,options).convertTo(ErrorDetectingCLIResult.class);
		if(status.asStatus().getSeverity() == IStatus.ERROR){
			throw new CoreException(status.asStatus());
		}
	}
	
	public CordovaProjectCLI getProjectCLI(){
		return CordovaProjectCLI.newCLIforProject(HybridProject.this);
	}
		
	public void updateDerivedFolders(IProgressMonitor monitor) throws CoreException{
		if(monitor == null){
			monitor = new NullProgressMonitor();
		}
		for(String derivedFolder: PlatformConstants.DERIVED_SUBFOLDERS){
			IFolder folder = getProject().getFolder(derivedFolder);
			if(folder != null && folder.exists()){
				for(IResource member: folder.members()){
					if(member instanceof IFolder){
						IFolder subFolder = (IFolder) member;
						setDerivedFolder(subFolder, monitor);
					}
				}
			}
		}
		for(String derivedFolder: PlatformConstants.DERIVED_FOLDERS){
			IFolder folder = getProject().getFolder(derivedFolder);
			setDerivedFolder(folder, monitor);
		}
	}
	
	private void setDerivedFolder(IFolder folder, IProgressMonitor monitor) throws CoreException{
		if(folder != null && folder.exists() && !folder.isDerived()){
			folder.setDerived(true, monitor);
		}
	}

	/**
	 * Returns the app name from the config.xml 
	 * if there is not one it falls back to project name.
	 * @return
	 */
	public String getAppName(){
		String name = null;
		try{
			WidgetModel widgetModel = WidgetModel.getModel(this);
			Widget w = widgetModel.getWidgetForRead();
			if(w != null){ 
				name = w.getName();
			}
		}catch(CoreException e){
			//let it come from project name
		}
		if(name == null || name.isEmpty()){
			name = kernelProject.getName();
		}
		return name;
	}
	
	/**
	 * Returns an app name that can be used to name build 
	 * artifacts. The returned name does not contain whitespaces.
	 * @return
	 */
	public String getBuildArtifactAppName(){
		String name = getAppName();
		name = name.replaceAll("\\W", "_");
		return name;
	}
	
	/**
	 * Returns the config.xml file for the project.
	 * This method searches all the valid locations for the config.xml file.
	 * May return null if one does not exist.
	 * @return
	 */
	public IFile getConfigFile(){
		for (IPath configPath : PlatformConstants.CONFIG_PATHS) {
			IFile f = kernelProject.getFile(configPath);
			if(f.exists()){
				return f;
			}
		}
		return null;
	}
	
	public HybridMobileEngineManager getEngineManager(){
		if (this.engineManager == null ){
			engineManager = new HybridMobileEngineManager(this);
		}
		return engineManager;
	}

	@Override
	public boolean equals(Object obj) {
		if(this.kernelProject == null )
			return super.equals(obj);
		if(obj == null ) 
			return false;
		if(!(obj instanceof HybridProject))
			return false;
		IProject prj = ((HybridProject)obj).getProject();
		return kernelProject.equals(prj);
	}
	
	@Override
	public int hashCode() {
		if(kernelProject == null )
			return super.hashCode();
		return kernelProject.hashCode();
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if( kernelProject == null )
			return null;
		if(adapter.isInstance(IProject.class))
			return kernelProject;
		return kernelProject.getAdapter(adapter);
	}
}
