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

import static org.eclipse.thym.core.internal.util.FileUtils.toURL;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridMobileStatus;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.HybridMobileLibraryResolver;
import org.eclipse.thym.core.engine.PlatformLibrary;
import org.eclipse.thym.core.internal.util.FileUtils;
import org.eclipse.thym.core.plugin.CordovaPluginManager;
import org.eclipse.thym.core.plugin.FileOverwriteCallback;
import org.osgi.framework.Bundle;
/**
 * Abstract class for all the native project generators delegates.
 * 
 * @author Gorkem Ercan
 *
 */
public abstract class AbstractProjectGeneratorDelegate {

	private final static String ASSEMBLY_ROOT = "/proj_gen/";
	
	private IProject project;
	private File generationRoot;
	private String platform;

	/**
	 * Constructs a project generator. 
	 */
	public AbstractProjectGeneratorDelegate(){

	}
	
	/**
	 * Initializes this ProjectGenerator. init must be called before calling the 
	 * generateNow. generationFolder can be null. If generationFolder is null generation folder 
	 * defaults to a folder created under the {@link Bundle} dataFile folder.
	 * 
	 * @param project
	 * @param generationFolder
	 */
	public void init(IProject project, File generationFolder, String platformID )
	{
		this.project = project;
		this.generationRoot = generationFolder;
		this.platform = platformID;
		if(generationRoot == null ){
			generationRoot = new File(getTempGenerationDirectory(), project.getName());
		}
	}
	/**
	 * Starts the target platform project generation.
	 * 
	 * @param monitor
	 * @return the location of the generated project
	 * @throws CoreException
	 */
	public File generateNow(IProgressMonitor monitor) throws CoreException{
		if(project == null || generationRoot == null )
			throw new IllegalStateException("Project generator delegate is not initialized properly");
		long start = System.currentTimeMillis();
		try {
			if(!generationRoot.exists() && !generationRoot.mkdirs() ){
				throw new CoreException(new Status(IStatus.ERROR,HybridCore.PLUGIN_ID, 
						NLS.bind("Can not create the destination directory for project generation at {0}",generationRoot.toString()) ));
			}
			monitor.beginTask(NLS.bind("Generate Native Project for {0}",this.getProjectName()), 50);
			HybridProject hybridProject = HybridProject.getHybridProject(getProject());
			HybridMobileEngine engine = hybridProject.getActiveEngine();
			if(engine == null){
				throw new CoreException(HybridMobileStatus.newMissingEngineStatus(project, 
					"Active Hybrid Mobile Engine is missing. Please install the missing engine or use a different engine."));
			}
			PlatformLibrary lib = engine.getPlatformLib(getTargetShortName());
			if(lib == null ){
				throw new CoreException(HybridMobileStatus.newMissingEngineStatus(getProject(),
						NLS.bind("Active Hybrid Mobile Engine for {0} project does not have {1} support installed.",new Object[]{getProject().getName(), getTargetShortName()}) ));
			}
			
			HybridMobileLibraryResolver resolver = lib.getPlatformLibraryResolver();
			if(resolver == null ){
				throw new CoreException(HybridMobileStatus.newMissingEngineStatus(getProject(),
						NLS.bind("Active Hybrid Mobile Engine can not support {0}.",getTargetShortName()) ));
			}
			if(resolver.needsPreCompilation()){
				resolver.preCompile(monitor);
			}
			IStatus libStatus = resolver.isLibraryConsistent();
			if(!libStatus.isOK()){
				throw new CoreException(HybridMobileStatus.newMissingEngineStatus(project, 
						"Active Hybrid Mobile Engine is missing or not compatible. Please install or use a different engine."));
			}
			generateNativeFiles(resolver);
			monitor.worked(10);
			IFolder folder = getProject().getFolder("/"+PlatformConstants.DIR_WWW);
			if ( !folder.isAccessible()){
				throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "www folder is missing. Can not generate target project without www directory"));
			}
			File targetWWW = getPlatformWWWDirectory();
			Assert.isNotNull(targetWWW,"Platform implementation must return a file location for www directory");
			if( !targetWWW.exists() && !targetWWW.mkdirs() ){
				throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, " Unable to create www directory for native project "));
			}
			copyResource(folder, folder.getFullPath(), targetWWW);
			monitor.worked(10);
			folder = getProject().getFolder("/"+ PlatformConstants.DIR_MERGES+"/"+getTargetShortName());
			if (folder.exists()){
				copyResource(folder, folder.getFullPath(), targetWWW);
			}
			monitor.worked(10);
			
			// Copy config.xml if it is not already exists.
			// this is needed to support config.xml files to live on the root of the project.
			File configDestination = new File(targetWWW, PlatformConstants.FILE_XML_CONFIG);
			if(!configDestination.exists()){
				File sourceFile = hybridProject.getConfigFile().getLocation().toFile();
				FileUtils.fileCopy(toURL(sourceFile), toURL(configDestination));
			}
			
			replaceCordovaPlatformFiles(resolver);
			completeCordovaPluginInstallations(monitor);
		}
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, " Unable to generate native project ",e));
		}
		finally{
			monitor.done();
		}
		HybridCore.trace(getTargetShortName()+ " project generated in " + Long.toString(System.currentTimeMillis() - start) +" ms.");
		return getDestination();
	}
	
	private void copyResource(final IResource resource, final IPath workspaceRoot, final File root) throws IOException{
		if(!resource.isAccessible()) return;
		
		File destinationFile = createDestinationFile( resource, workspaceRoot, root);
		if(resource.getType() == IResource.FILE ){
			File sourceFile = resource.getLocation().toFile();
			FileUtils.fileCopy(toURL(sourceFile), toURL(destinationFile));
		}else{
			IContainer container = (IContainer) resource;
			IResource[] children = null;
			try {
				children = container.members();
			} catch (CoreException e) {
				// isAccessible() call before is guarding this, should never happen.
				HybridCore.log(IStatus.ERROR, "Copying resources to generated project", e);
			}
			
			destinationFile.mkdir();
			if (children != null) {
				for (IResource child : children) {
					copyResource(child, workspaceRoot, root);
				}
			}
		}
	}
	
	
	private File createDestinationFile(IResource resource, IPath workspaceRoot, File root) {
		return new File(root, resource.getFullPath().makeRelativeTo(workspaceRoot).toOSString());
	}

	protected void completeCordovaPluginInstallations(IProgressMonitor monitor) throws CoreException{
		HybridProject project = HybridProject.getHybridProject(getProject());
		if( project == null ) return;
		CordovaPluginManager pluginManager = new CordovaPluginManager(project);
		pluginManager.completePluginInstallationsForPlatform(getDestination(), getTargetShortName(),new FileOverwriteCallback() {
			
			@Override
			public boolean isOverwiteAllowed(String[] files) {
				return true;
			}
		},monitor);
	}

	/**
	 * Template method to be implemented by the platform implementations. 
	 * Platform implementations should generate native project files 
	 * and the Cordova Library. This method is called before moving the 
	 * web artifacts in www directory.
	 * 
	 * @param resolver to be used to retrieve engine files.
	 * @throws IOException
	 */
	protected abstract void generateNativeFiles(HybridMobileLibraryResolver resolver) throws CoreException;
	
	/**
	 * Returns the short name to be used for defining the target platform 
	 * such as <i>ios, android</i> etc.
	 * @return
	 */
	protected String getTargetShortName(){
		return platform;
	}
	
	/**
	 * Template method to be implemented by the platform implementations. 
	 * This method is called after the web artifacts are copied to the proper location 
	 * on the generated project to give platform implementation a chance to replace 
	 * platform specific Apache Cordova artifacts such as the cordova.js
	 *  
	 * @param resolver to be used to retrieve engine files.
	 * @throws IOException
	 */
	protected abstract void replaceCordovaPlatformFiles(HybridMobileLibraryResolver resolver) throws IOException; 
	
	/**
	 * Returns the platform specific location of the www directory. 
	 * This is used to merge and copy project resources to native project.
	 * @return File that points to the www directory on native project
	 */
	protected abstract File getPlatformWWWDirectory();
	
	
	/**
	 * The destination folder where the generated files are replaced. This is 
	 * usually a directory named the short name for a target platform under a 
	 * a root folder for project.
	 * @return
	 */
	public File getDestination(){
		return new File(generationRoot,getTargetShortName());
	}
	protected String getProjectName(){
		return project.getName();
	}
	
	protected IProject getProject(){
		return project;
	}
	
	protected File getTempGenerationDirectory(){
		Bundle bundle = HybridCore.getContext().getBundle();
		return bundle.getDataFile(ASSEMBLY_ROOT);
	}
}
