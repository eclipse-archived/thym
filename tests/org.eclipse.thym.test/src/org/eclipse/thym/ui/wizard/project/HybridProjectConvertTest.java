/*******************************************************************************
 * Copyright (c) 2015, 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *     Contributors:
 *          Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.wizard.project;


import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.extensions.PlatformSupport;
import org.eclipse.thym.core.natures.HybridAppNature;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.hybrid.test.RequiresCordovaCLICategory;
import org.eclipse.wst.jsdt.core.IIncludePathEntry;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@SuppressWarnings("restriction")
@RunWith(Parameterized.class)
public class HybridProjectConvertTest {

    private static final String PROJECT_NAME = "ConvertTest";
    
    @Parameters(name = "{index}: project with config {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {     
                 {"/res/configWithoutEngine.xml"}, {"/res/config.xml"}  
           });
    }
    
    @Parameter
    public String configXmlLocation;

    @Before
    public void createBaseProject() throws CoreException{
        
        IProject newProject = getTheProject();
        
        if ( !newProject.exists() ){
            IProjectDescription description = newProject.getWorkspace().newProjectDescription(PROJECT_NAME);
            
            newProject.create(description, new NullProgressMonitor());
            if( !newProject.isOpen() ){
                newProject.open(new NullProgressMonitor());
            }
        }
    }
    
    @After
    public void cleanBaseProject() throws CoreException{ 
        IProject project = getTheProject();
        if(project.exists()){
            project.delete(true, new NullProgressMonitor());
        }
    }
    
    @Test(expected = CoreException.class)
    @Category(value=RequiresCordovaCLICategory.class)
    public void testCanConvertCausesException() throws CoreException{
        IProject project = getTheProject();
        assertTrue(project.exists());
        HybridProjectCreator creator = new HybridProjectCreator();
        creator.convertProject(project, new NullProgressMonitor());
    }
    
    @Test
    @Category(value=RequiresCordovaCLICategory.class)
    public void testNature() throws CoreException{
        IProject project = getTheProject();
        addRequiredResources(project);
        HybridProjectCreator creator = new HybridProjectCreator();
        creator.convertProject(project, new NullProgressMonitor());
        assertTrue("Missing Hybrid project nature id",project.hasNature(HybridAppNature.NATURE_ID));
        assertTrue("Missing JavaScript project nature id", project.hasNature(JavaScriptCore.NATURE_ID));
        HybridProject hp = HybridProject.getHybridProject(project);
        assertNotNull("Can not retrieve HybridProject instance", hp);
    }

    
    @Test
    @Category(value=RequiresCordovaCLICategory.class)
    public void directoryStructureTest() throws CoreException{
        IProject theProject = getTheProject();
        addRequiredResources(theProject);
        HybridProjectCreator creator = new HybridProjectCreator();
        creator.convertProject(theProject, new NullProgressMonitor());
        
        
        String[] paths={ ".cordova", PlatformConstants.DIR_MERGES, "plugins", PlatformConstants.DIR_WWW };
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
    @Category(value=RequiresCordovaCLICategory.class)
    public void testJavaScriptProjectSetup() throws CoreException{
        IProject theProject = getTheProject();
        addRequiredResources(theProject);
        HybridProjectCreator creator = new HybridProjectCreator();
        creator.convertProject(theProject, new NullProgressMonitor());
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
    
    private IProject getTheProject(){
           IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
           IProject project = workspaceRoot.getProject(PROJECT_NAME);
           return project;
    }

    private void addRequiredResources(IProject project) throws CoreException {
        IFolder folder = project.getFolder(PlatformConstants.DIR_WWW);
        assertFalse(folder.exists());
        folder.create(true, true, new NullProgressMonitor());
        IFile file = project.getFile(PlatformConstants.FILE_XML_CONFIG);
        assertFalse(file.exists());
        InputStream configXML = this.getClass().getResourceAsStream(configXmlLocation);
        assertNotNull(configXML);
        file.create(configXML,true,new NullProgressMonitor());
    }
    
}
