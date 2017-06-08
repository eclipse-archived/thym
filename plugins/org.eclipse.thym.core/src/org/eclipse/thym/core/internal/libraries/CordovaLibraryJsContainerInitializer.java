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
package org.eclipse.thym.core.internal.libraries;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.HybridMobileLibraryResolver;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.wst.jsdt.core.IIncludePathEntry;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJsGlobalScopeContainer;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.JsGlobalScopeContainerInitializer;
import org.eclipse.wst.jsdt.core.compiler.libraries.LibraryLocation;
import org.eclipse.wst.jsdt.core.infer.DefaultInferrenceProvider;

public class CordovaLibraryJsContainerInitializer extends JsGlobalScopeContainerInitializer {
	public static final String CONTAINER_ID = "org.eclipse.thym.core.CordovaContainerInitializer";
	private IJavaScriptProject project;

	public CordovaLibraryJsContainerInitializer() {
		// we do not want super()
	}
	
	public CordovaLibraryJsContainerInitializer(IJavaScriptProject project){
		this.project = project;
	}
	
	@Override
	public void initialize(IPath containerPath, IJavaScriptProject project)
			throws CoreException {
		CordovaLibraryJsContainerInitializer scopeContainer = new CordovaLibraryJsContainerInitializer(project);
		JavaScriptCore.setJsGlobalScopeContainer(containerPath, new IJavaScriptProject[] { project }, new IJsGlobalScopeContainer[] {scopeContainer} , null);
	}
	
	@Override
	public String getDescription() {
		return "Cordova JS Library";
	}
	
	@Override
	public IPath getPath() {
		return new Path(CONTAINER_ID);
	}
	
	@Override
	public int getKind() {
		return IJsGlobalScopeContainer.K_DEFAULT_SYSTEM;
	}
	
	@Override
	public IIncludePathEntry[] getIncludepathEntries() {
		List<IIncludePathEntry> list = new ArrayList<IIncludePathEntry>();

		IIncludePathEntry entry = getCordovaJsIncludePathEntry();
		if (entry != null) // We shouldn't return 'null'-entries
			list.add(entry);
		
		entry = getPluginJsIncludePathEntry();
		if (entry != null) // We shouldn't return 'null'-entries
			list.add(entry);
		
		return list.toArray(new IIncludePathEntry[list.size()]);
	}
	
	private IIncludePathEntry getPluginJsIncludePathEntry() {
		try {
			String content = "cordova.define('cordova/plugin_list', function(require, exports, module) {"
				+"module.exports = [ ] });";
			String projectName = "cordova_generic";
			if(project != null ){ 
				HybridProject prj = HybridProject.getHybridProject(project.getProject());
				if(prj != null){
					content = prj.getPluginManager().getCordovaPluginJSContent(null);
				}
				projectName = project.getProject().getName();
			}
			IPath pluginJSRuntimePath = getLibraryRuntimeFolder().append(projectName).append(PlatformConstants.FILE_JS_CORDOVA_PLUGIN);
			InputStream is = new ByteArrayInputStream(content.getBytes());
			File pluginJs = pluginJSRuntimePath.toFile();
			FileUtils.copyInputStreamToFile(is, pluginJs);
			return JavaScriptCore.newLibraryEntry(pluginJSRuntimePath.makeAbsolute(),null, null);
			
		} catch (CoreException e) {
			HybridCore.log(IStatus.ERROR, "Error creating the cordova plugin JS runtime libraries", e);	
		} catch (IOException e) {
			HybridCore.log(IStatus.ERROR, "Error creating the cordova plugin JS runtime libraries", e);	
		}
		return null;
	}

	private IIncludePathEntry getCordovaJsIncludePathEntry(){
		try {
			IPath cordovaJSRuntimePath = getLibraryRuntimeFolder().append(PlatformConstants.FILE_JS_CORDOVA);
			File cordovaJS = cordovaJSRuntimePath.toFile();
			if (!cordovaJS.exists()) {
				HybridProject prj = HybridProject.getHybridProject(project.getProject());
				
				HybridMobileEngine[] activeEngines = prj.getEngineManager().getEngines();
				if(activeEngines == null || activeEngines.length <1){
					return null;
				}
				HybridMobileLibraryResolver resolver = activeEngines[0].getResolver();
				if(resolver != null){
					String templateCordovaJS = resolver.getTemplateFile(PlatformConstants.FILE_JS_CORDOVA);
				
					File jsFile = new File(project.getProject().getFile(new Path("platforms/"+templateCordovaJS.toString())).getRawLocation().toOSString());
				
					if(jsFile.exists()){
						org.eclipse.thym.core.internal.util.FileUtils.fileCopy(
								org.eclipse.thym.core.internal.util.FileUtils.toURL(jsFile),
								org.eclipse.thym.core.internal.util.FileUtils.toURL(cordovaJS));

					}
				}
			}
			return JavaScriptCore.newLibraryEntry(cordovaJSRuntimePath.makeAbsolute(),null, null);
			
		} catch (IOException e) {
			HybridCore.log(IStatus.ERROR, "Error creating the cordova JS runtime libraries", e);
		}
		return null;
	}
	
	
	private IPath getLibraryRuntimeFolder(){
		IPath libraryRuntimePath = Platform.getStateLocation(Platform.getBundle(HybridCore.PLUGIN_ID)).append("cordovaJsLib");
		File libRuntimeFile = libraryRuntimePath.toFile();
		if(!libRuntimeFile.isDirectory()){
			libRuntimeFile.mkdir();
		}
		return libraryRuntimePath;

	}

	@Override
	public String[] resolvedLibraryImport(String realImport) {
		return new String[] {realImport};
	}


	@Override
	public boolean canUpdateJsGlobalScopeContainer(IPath containerPath,
			IJavaScriptProject project) {
		return true;
	}

	@Override
	public void requestJsGlobalScopeContainerUpdate(IPath containerPath,
			IJavaScriptProject project,
			IJsGlobalScopeContainer containerSuggestion) throws CoreException {
	}

	@Override
	public String getDescription(IPath containerPath, IJavaScriptProject project) {
		if(containerPath.toOSString().endsWith(PlatformConstants.FILE_JS_CORDOVA)){
			return "Cordova Engine Library";
		} else if(containerPath.toOSString().endsWith(PlatformConstants.FILE_JS_CORDOVA_PLUGIN)){
			return "Cordova Plugin Library";
		}
		return "Cordova JS Library";
	}

	@Override
    public IJsGlobalScopeContainer getFailureContainer(final IPath containerPath, IJavaScriptProject project) {
    	final String description = getDescription(containerPath, project);
    	return
    		new IJsGlobalScopeContainer() {
				public IIncludePathEntry[] getIncludepathEntries() {
					return new IIncludePathEntry[0];
				}
				public String getDescription() {
					return description;
				}
				public int getKind() {
					return 0;
				}
				public IPath getPath() {
					return containerPath;
				}
				public String toString() {
					return getDescription();
				}
				public String[] resolvedLibraryImport(String a) {
					return new String[] {a};
				}
			};
	}

	@Override
	public Object getComparisonID(IPath containerPath,
			IJavaScriptProject project) {
		if (containerPath == null) {
			return null;
		} else {
			return containerPath.segment(0);
		}
	}

	@Override
	public URI getHostPath(IPath path, IJavaScriptProject project) {
		return null;
	}

	@Override
	public boolean allowAttachJsDoc() {
		return true;
	}

	@Override
	public String[] containerSuperTypes() {
		return new String[] {"cordova"};
	}

	@Override
	public String getInferenceID() {
		return DefaultInferrenceProvider.ID;
	}

	@Override
	public void removeFromProject(IJavaScriptProject project) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LibraryLocation getLibraryLocation() {
		return null;
	}
	

}
