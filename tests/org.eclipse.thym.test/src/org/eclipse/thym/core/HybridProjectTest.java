/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.times;
import static org.mockito.Matchers.anyInt;

import static org.mockito.Mockito.verify;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy2;
import org.eclipse.thym.core.engine.HybridMobileEngineManager;
import org.eclipse.thym.core.internal.cordova.CordovaCLIResult;
import org.eclipse.thym.core.internal.cordova.CordovaProjectCLI;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.ui.wizard.project.HybridProjectCreator;
import org.junit.BeforeClass;
import org.junit.Test;

public class HybridProjectTest {
	
	private static final String PROJECT_NAME = "TestProject";
	private static final String APP_NAME = "Test App";
    private static final String APP_ID = "Test.id";
    
	private IProject mockEclipseProject;
	private HybridProject mockProject;
	
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
	public void testGetProject(){
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject theProject = workspaceRoot.getProject(PROJECT_NAME);
		HybridProject hProject = HybridProject.getHybridProject(theProject);
		assertNotNull(hProject.getProject()); 
	}
	
	@Test
	public void testGetCLI(){
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject theProject = workspaceRoot.getProject(PROJECT_NAME);
		HybridProject hProject = HybridProject.getHybridProject(theProject);
		assertNotNull(hProject.getProjectCLI()); 
	}
	
	@Test
	public void testGetPluginManager(){
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject theProject = workspaceRoot.getProject(PROJECT_NAME);
		HybridProject hProject = HybridProject.getHybridProject(theProject);
		assertNotNull(hProject.getPluginManager()); 
	}
	
	@Test
	public void testGetEngineManager(){
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject theProject = workspaceRoot.getProject(PROJECT_NAME);
		HybridProject hProject = HybridProject.getHybridProject(theProject);
		assertNotNull(hProject.getEngineManager()); 
	}
	
	@Test
	public void testGetHybridProject() throws CoreException{
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
				IProject newProject = workspaceRoot.getProject("basicProject");
				if (!newProject.exists()) {
					IProjectDescription description = newProject.getWorkspace().newProjectDescription("basicProject");
					newProject.create(description, monitor);
					if (!newProject.isOpen()) {
						newProject.open(monitor);
					}
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, null);
		
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject nonHybridProject = workspaceRoot.getProject("basicProject");
		IProject hybridProject = workspaceRoot.getProject(PROJECT_NAME);
		
		
		assertNull(HybridProject.getHybridProject((IProject)null));
		assertNull(HybridProject.getHybridProject(nonHybridProject)); // does not have hybrid mobile nature
		assertNull(HybridProject.getHybridProject((String)null));
		assertNull(HybridProject.getHybridProject("non-existing-projec"));
		assertNull(HybridProject.getHybridProject("basicProject")); // does not have hybrid mobile nature
		assertNotNull(HybridProject.getHybridProject(hybridProject));
		assertNotNull(HybridProject.getHybridProject(PROJECT_NAME));
	}
	
	@Test
	public void testProjectPrepare() throws CoreException{
		setupProjectMock();
		CordovaProjectCLI cli = setupMockCLI(mockProject);
		mockProject.prepare(new NullProgressMonitor(), "");
		verify(cli).prepare(any(IProgressMonitor.class), eq(""));
		verify(mockEclipseProject).refreshLocal(anyInt(), any(IProgressMonitor.class));
	}
	
	@Test
	public void testProjectPrepareWithError() throws CoreException{
		setupProjectMock();
		CordovaProjectCLI cli = setupMockCLI(mockProject);
		CordovaCLIResult result =  new CordovaCLIResult("Error: some_error");
		doReturn(result).when(cli).prepare(any(IProgressMonitor.class), any(String.class));
		try{
			mockProject.prepare(new NullProgressMonitor(), "");
		} catch (CoreException e) {
			assertEquals("some_error", e.getStatus().getMessage());
			return;
		}
		fail("prepare should throw CoreException");
	}
	
	@Test
	public void testProjectBuild() throws CoreException{
		setupProjectMock();
		CordovaProjectCLI cli = setupMockCLI(mockProject);
		mockProject.build(new NullProgressMonitor(), "");
		verify(cli).build(any(IProgressMonitor.class), eq(""));
		verify(mockEclipseProject).refreshLocal(anyInt(), any(IProgressMonitor.class));
	}
	
	@Test
	public void testProjectBuildWithError() throws CoreException{
		setupProjectMock();
		CordovaProjectCLI cli = setupMockCLI(mockProject);
		CordovaCLIResult result =  new CordovaCLIResult("Error: some_error");
		doReturn(result).when(cli).build(any(IProgressMonitor.class), any(String.class));
		try{
			mockProject.build(new NullProgressMonitor(), "");
		} catch (CoreException e) {
			assertEquals("some_error", e.getStatus().getMessage());
			return;
		}
		fail("build should throw CoreException");
	}
	
	@Test
	public void testProjectEmulate() throws CoreException{
		setupProjectMock();
		CordovaProjectCLI cli = setupMockCLI(mockProject);
		mockProject.emulate(new NullProgressMonitor(), "");
		verify(cli).emulate(any(IProgressMonitor.class), eq(""));
		verify(mockEclipseProject, times(0)).refreshLocal(anyInt(), any(IProgressMonitor.class));
	}
	
	@Test
	public void testProjectEmulateWithError() throws CoreException{
		setupProjectMock();
		CordovaProjectCLI cli = setupMockCLI(mockProject);
		CordovaCLIResult result =  new CordovaCLIResult("Error: some_error");
		doReturn(result).when(cli).emulate(any(IProgressMonitor.class), any(String.class));
		try{
			mockProject.emulate(new NullProgressMonitor(), "");
		} catch (CoreException e) {
			assertEquals("some_error", e.getStatus().getMessage());
			return;
		}
		fail("emulate should throw CoreException");
	}
	
	@Test
	public void testProjectRun() throws CoreException{
		setupProjectMock();
		CordovaProjectCLI cli = setupMockCLI(mockProject);
		mockProject.run(new NullProgressMonitor(), "");
		verify(cli).run(any(IProgressMonitor.class), eq(""));
		verify(mockEclipseProject, times(0)).refreshLocal(anyInt(), any(IProgressMonitor.class));
	}
	
	@Test
	public void testProjectRunWithError() throws CoreException{
		setupProjectMock();
		CordovaProjectCLI cli = setupMockCLI(mockProject);
		CordovaCLIResult result =  new CordovaCLIResult("Error: some_error");
		doReturn(result).when(cli).run(any(IProgressMonitor.class), any(String.class));
		try{
			mockProject.run(new NullProgressMonitor(), "");
		} catch (CoreException e) {
			assertEquals("some_error", e.getStatus().getMessage());
			return;
		}
		fail("run should throw CoreException");
	}
	
	@Test
	public void testProjectDerivedFolders() throws CoreException{
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject theProject = workspaceRoot.getProject(PROJECT_NAME);
		HybridProject hProject = HybridProject.getHybridProject(theProject);
		createFolders(hProject);
		hProject.updateDerivedFolders(new NullProgressMonitor());
		
		for(String folderName: PlatformConstants.DERIVED_SUBFOLDERS){
			checkDerivedSubFolders(hProject, folderName);
		}
		for(String folderName: PlatformConstants.DERIVED_FOLDERS){
			IFolder folder = hProject.getProject().getFolder(folderName);
			assertTrue(folder.isDerived());
		}
	}
	
	private void checkDerivedSubFolders(HybridProject hProject, String folderName){
		IFolder folder = hProject.getProject().getFolder(folderName);
		IFolder subFolder1 = folder.getFolder(folder.getName()+"0");
		assertTrue(subFolder1.isDerived());
		IFolder subFolder2 = folder.getFolder(folder.getName()+"1");
		assertTrue(subFolder2.isDerived());
		
		IFile subFile1 = folder.getFile("file0");
		assertFalse(subFile1.isDerived());
		
		IFile subFile2 = folder.getFile("file1");
		assertFalse(subFile2.isDerived());
	}
	
	private void createFolders(HybridProject hProject) throws CoreException{
		for(String folderName: PlatformConstants.DERIVED_FOLDERS){
			IFolder folder = hProject.getProject().getFolder(new Path(folderName));
			if(!folder.exists()){
				folder.create(true, true, new NullProgressMonitor());
			}
		}
		for(int i=0; i<PlatformConstants.DERIVED_SUBFOLDERS.length; i++){
			String folderName = PlatformConstants.DERIVED_SUBFOLDERS[i];
			IFolder folder = hProject.getProject().getFolder(new Path(folderName));
			if(!folder.exists()){
				folder.create(true, true, new NullProgressMonitor());
			}
			//create 2 subfolders and 2 files
			for(int y=0;y<2;y++){
				IFolder subFolder = hProject.getProject().getFolder(new Path(folderName+"/"+folderName+y));
				if(!subFolder.exists()){
					subFolder.create(true, true, new NullProgressMonitor());
				}
				IFile subFile = hProject.getProject().getFile(new Path(folderName+"/file"+y));
				if(!subFile.exists()){
					subFile.create(null, true, new NullProgressMonitor());
				}
			}
		}
		hProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
	}
	
	private CordovaProjectCLI setupMockCLI(HybridProject project) throws CoreException {
		IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
		CordovaProjectCLI mockCLI = spy(CordovaProjectCLI.newCLIforProject(mockProject));
		
		doReturn(mockProcess).when(mockCLI).startShell(any(IStreamListener.class), any(IProgressMonitor.class),any(ILaunchConfiguration.class));
		
		when(mockProcess.getStreamsProxy()).thenReturn(mockStreams);
		when(mockProcess.isTerminated()).thenReturn(Boolean.TRUE);
		
		doReturn(mockCLI).when(project).getProjectCLI();
		return mockCLI;
	}
	
	private void setupProjectMock() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject theProject = workspaceRoot.getProject(PROJECT_NAME);
		HybridProject hProject = HybridProject.getHybridProject(theProject);
		assertNotNull(hProject);
		mockProject = spy(hProject);
		mockEclipseProject = spy(mockProject.getProject());
		
		doReturn(mockEclipseProject).when(mockProject).getProject();
	}

}
