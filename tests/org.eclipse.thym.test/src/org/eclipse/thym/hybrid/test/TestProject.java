/*******************************************************************************
 * Copyright (c) 2013, 2016 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.hybrid.test;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.engine.HybridMobileEngineManager;
import org.eclipse.thym.core.natures.HybridAppNature;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.ui.wizard.project.HybridProjectCreator;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class TestProject {
	//copied from org.eclipse.thym.ui.wizard.project.HybridProjectCreator
	private static final String[] COMMON_PATHS={ ".cordova", PlatformConstants.DIR_MERGES, 
		PlatformConstants.DIR_PLUGINS,
		PlatformConstants.DIR_WWW };
	
	public static final String PROJECT_NAME = "HybridToolsTest";
	public static final String APPLICATION_NAME = "Test applciation";
	public static final String APPLICATION_ID = "hybrid.tools.test";
	
	@SuppressWarnings("restriction")
	public TestProject(){
		try {
			HybridProjectCreator projectCreator = new HybridProjectCreator();
			projectCreator.createBasicTemplatedProject(PROJECT_NAME, null, APPLICATION_NAME, APPLICATION_ID, 
					HybridMobileEngineManager.defaultEngines(), new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	
	private IStatus isProjectValid() throws CoreException{
		IProject project = getProject();
		if( !project.hasNature(HybridAppNature.NATURE_ID ) ){
			return error("project does not have hybrid application nature");
		}
		if( !project.hasNature( JavaScriptCore.NATURE_ID )){
			return error("project does not have javascript nature");
		}
		for (int i = 0; i < COMMON_PATHS.length; i++) {
			IResource resource = project.findMember(COMMON_PATHS[i]);
			if(resource == null || !resource.exists()){
				error("Project is missing "+ COMMON_PATHS[i] );
			}
		}
		Document doc;
		try {
			doc = loadConfigXML();
		} catch (Exception e) {
			return error("error parsing config.xml");
		}
		String id = doc.getDocumentElement().getAttribute("id");
		if( !APPLICATION_ID.equals(id)){
			error("wrong application id");
		}
		NodeList nodes = doc.getDocumentElement().getElementsByTagName("name");
		if(nodes.getLength()< 1){
			return error("Application name is not updated"); 
		}
		String name = nodes.item(0).getTextContent();
		if( !APPLICATION_NAME.equals(name)){
			return error("Wrong application name");
		}
		
		
		return Status.OK_STATUS;
	}


	public IProject getProject() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(PROJECT_NAME);
		return project;
	}
	
	public HybridProject hybridProject()
	{
		IProject prj = getProject();
		return HybridProject.getHybridProject(prj);
	}
	
	public void delete() throws CoreException{
		getProject().delete(true, true, new NullProgressMonitor());
	}

	private  Document loadConfigXML() throws Exception {
	    DocumentBuilder db;
		DocumentBuilderFactory dbf =DocumentBuilderFactory.newInstance();
	
	    	db = dbf.newDocumentBuilder();
	    	IFile file =  getProject().getFile("/www/config.xml");
	    	if(file == null )
	    		return null;
	    	return db.parse(file.getContents()); 
		
	}
	
	private Status error(String message) {
		return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
	}

	public void writePlatformsJson(String toWrite) throws CoreException {
		IFile file = getProject().getFile("/platforms/platforms.json");
		ByteArrayInputStream input = new ByteArrayInputStream(toWrite.getBytes());
		if (!file.exists()) {
			file.create(input, true, null);
		} else {
			file.setContents(input, true, false, null);
		}
	}

	public void deletePlatformsJson() throws CoreException {
		IFile file = getProject().getFile("/platforms/platforms.json");
		if (file.exists()) {
			file.delete(true, null);
		}
	}
}
