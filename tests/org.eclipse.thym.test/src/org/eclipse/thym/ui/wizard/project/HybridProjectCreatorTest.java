/*******************************************************************************
 * Copyright (c) 2013, 2015 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *     Contributors:
 *          Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.wizard.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.HybridMobileEngineManager;
import org.eclipse.thym.core.extensions.PlatformSupport;
import org.eclipse.thym.core.natures.HybridAppNature;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.wst.jsdt.core.IIncludePathEntry;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@SuppressWarnings("restriction")
public class HybridProjectCreatorTest {
    private static final String PROJECT_NAME = "TestProject";
    private static final String APP_NAME = "Test App";
    private static final String APP_ID = "Test.id";

    private IProject getTheProject() {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject theProject = workspaceRoot.getProject(PROJECT_NAME);
        return theProject;
    }

    @BeforeClass
    public static void createTestProject() throws CoreException{
    	IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				HybridProjectCreator creator = new HybridProjectCreator();
				creator.createBasicTemplatedProject(PROJECT_NAME, null, APP_NAME, APP_ID, 
						HybridMobileEngineManager.defaultEngines(),new NullProgressMonitor());
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, null);
    }

    @Test
    public void createProjectTest() {
        IProject theProject = getTheProject();
        assertTrue(theProject.exists());
    }

    @Test
    public void projectNatureTest() throws CoreException{
        IProject theProject = getTheProject();
        assertTrue(theProject.hasNature(HybridAppNature.NATURE_ID));
    }
    
    @Test
    public void directoryStructureTest(){
        IProject theProject = getTheProject();
        
        String[] paths={ ".cordova", PlatformConstants.DIR_MERGES, "plugins", PlatformConstants.DIR_WWW };//Copied from HybridProjectCreator
        for (int i = 0; i < paths.length; i++) {
            IFolder folder = theProject.getFolder( paths[i]);
            assertTrue(paths[i]+ " is not created. ", folder.exists());
        }
        List<PlatformSupport> platforms = HybridCore.getPlatformSupports();
        IPath merges = new Path(PlatformConstants.DIR_MERGES);
        for (PlatformSupport platform : platforms) {
            IPath platformDir = merges.append(platform.getPlatformId());
            IFolder folder = theProject.getFolder(platformDir);
            assertTrue(platformDir+ " is not created. ", folder.exists());
        }
    }
    
    @Test
    public void essentialFilesTest(){
        IProject theProject = getTheProject();
        IFile file = theProject.getFile("/www/config.xml");
        assertTrue(file.exists());
        file= theProject.getFile("/www/index.html");
        assertTrue(file.exists());
    }
    
    private  Document loadConfigXML() throws Exception {
        DocumentBuilder db;
        DocumentBuilderFactory dbf =DocumentBuilderFactory.newInstance();
    
            db = dbf.newDocumentBuilder();
            IFile file =  getTheProject().getFile("/www/config.xml");
            if(file == null )
                return null;
            return db.parse(file.getContents()); 
    }
    
    @Test
    public void configUpdatesTest() throws Exception{
        Document doc;
        doc = loadConfigXML();
        
        String id = doc.getDocumentElement().getAttribute("id");
        assertEquals(APP_ID, id);
        NodeList nodes = doc.getDocumentElement().getElementsByTagName("name");
        assertTrue(nodes.getLength()> 0);
        String name = nodes.item(0).getTextContent();
        assertEquals(APP_NAME, name);
    }
    
    @Test
    public void testJavaScriptProjectSetup() throws CoreException{
        IProject theProject = getTheProject();
        IJavaScriptProject javascriptProject = JavaScriptCore.create(theProject);
        IIncludePathEntry[] entries = javascriptProject.getRawIncludepath();
        List<IIncludePathEntry> entryList = new ArrayList<IIncludePathEntry>(Arrays.asList(entries));
        
        boolean foundWWW = false;
        for (IIncludePathEntry aEntry : entryList) {
            if(IIncludePathEntry.CPE_SOURCE == aEntry.getEntryKind() && 
                    aEntry.getPath().lastSegment().equals(PlatformConstants.DIR_WWW)){
                foundWWW = true;
            }
        }
        
        assertTrue("www is not configured to be a JavaScript source directory", foundWWW);
        
    }
    
}
