/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.internal.cordova;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy2;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.engine.HybridMobileEngineManager;
import org.eclipse.thym.core.internal.cordova.CordovaProjectCLI.Command;
import org.eclipse.thym.ui.wizard.project.HybridProjectCreator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("restriction")
public class CordovaProjectCLITest {
    private static final String PROJECT_NAME = "TestProject";
    private static final String APP_NAME = "Test App";
    private static final String APP_ID = "Test.id";
    
	
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

    @Test(expected=IllegalArgumentException.class)
    public void testNullProject(){
    	CordovaProjectCLI.newCLIforProject(null);
    }
    
    @Test
    public void testAdditionalEnvProperties() throws CoreException{
    	CordovaProjectCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);
    	ArgumentCaptor<ILaunchConfiguration> confCaptor = ArgumentCaptor.forClass(ILaunchConfiguration.class);
    	mockCLI.prepare(new NullProgressMonitor(), "android");
    	verify(mockCLI).startShell(any(IStreamListener.class), any(IProgressMonitor.class), confCaptor.capture());
    	Map<String,String> attr = confCaptor.getValue().getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map<String,String>)null);
    	assertEquals(EnvironmentPropsExt.ENV_VALUE, attr.get(EnvironmentPropsExt.ENV_KEY));
    }
    
    @Test
    public void testGeneratedPrepareCommandCorrectly() throws CoreException, IOException{
    	CordovaProjectCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);
    	mockCLI.prepare(new NullProgressMonitor(), "android");
    	verify(mockStreams).write("cordova prepare android\n");
    }
    
    @Test
    public void testGeneratedPluginCommandCorrectlyForADD() throws CoreException,IOException{
    	CordovaProjectCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.plugin(Command.ADD, new NullProgressMonitor(), "cordova-plugin-console@1.0.1", CordovaProjectCLI.OPTION_SAVE);
    	verify(mockStreams).write("cordova plugin add cordova-plugin-console@1.0.1 --save\n");
    }
    
    @Test
    public void testGeneratedPluginCommandCorrectlyForUPDATE() throws CoreException,IOException{
    	CordovaProjectCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.plugin(Command.UPDATE, new NullProgressMonitor(), "cordova-plugin-console@1.0.1", CordovaProjectCLI.OPTION_SAVE);
    	verify(mockStreams).write("cordova plugin update cordova-plugin-console@1.0.1 --save\n");
    }
    
    @Test
    public void testGeneratedPluginCommandCorrectlyForREMOVE() throws CoreException,IOException{
    	CordovaProjectCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.plugin(Command.REMOVE, new NullProgressMonitor(), "cordova-plugin-console", CordovaProjectCLI.OPTION_SAVE);
    	verify(mockStreams).write("cordova plugin remove cordova-plugin-console --save\n");
    }
    
    @Test
    public void testGeneratedPlatformCommandCorrectlyForADD() throws CoreException,IOException{
    	CordovaProjectCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.platform(Command.ADD, new NullProgressMonitor(), "ios@5.0.0", CordovaProjectCLI.OPTION_SAVE);
    	verify(mockStreams).write("cordova platform add ios@5.0.0 --save\n");
    }
    
    @Test
    public void testGeneratedPlatformCommandCorrectlyForUPDATE() throws CoreException,IOException{
    	CordovaProjectCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.platform(Command.UPDATE, new NullProgressMonitor(), "ios@5.0.0", CordovaProjectCLI.OPTION_SAVE);
    	verify(mockStreams).write("cordova platform update ios@5.0.0 --save\n");
    }
    
    @Test
    public void testGeneratedPlatformCommandCorrectlyForREMOVE() throws CoreException,IOException{
    	CordovaProjectCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.platform(Command.REMOVE, new NullProgressMonitor(), "ios", CordovaProjectCLI.OPTION_SAVE);
    	verify(mockStreams).write("cordova platform remove ios --save\n");
    }    

    @Test
    public void testGeneratedBuildCommandCorrectly() throws CoreException,IOException{
    	CordovaProjectCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.build(new NullProgressMonitor(), "ios");
    	verify(mockStreams).write("cordova build ios\n");
    }
    
    @Test
    public void testGeneratedRunCommandCorrectly() throws CoreException,IOException{
    	CordovaProjectCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.run(new NullProgressMonitor(), "android");
    	verify(mockStreams).write("cordova run android\n");
    }
    
    @Test
    public void testGeneratedEmulateCommandCorrectly() throws CoreException,IOException{
    	CordovaProjectCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.emulate(new NullProgressMonitor(), "windows");
    	verify(mockStreams).write("cordova emulate windows\n");
    }
    
	private void setupMocks(CordovaCLI mockCLI, IProcess mockProcess, IStreamsProxy2 mockStreams) throws CoreException {
		doReturn(mockProcess).when(mockCLI).startShell(any(IStreamListener.class), any(IProgressMonitor.class),any(ILaunchConfiguration.class));
		when(mockProcess.getStreamsProxy()).thenReturn(mockStreams);
		when(mockProcess.isTerminated()).thenReturn(Boolean.TRUE);
	}

	private CordovaProjectCLI getMockCLI() {
		CordovaProjectCLI mockCLI = spy(CordovaProjectCLI.newCLIforProject(getTheProject()));
		return mockCLI;
	}
	
	private HybridProject getTheProject() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject theProject = workspaceRoot.getProject(PROJECT_NAME);
		HybridProject hProject = HybridProject.getHybridProject(theProject);
		assertNotNull(hProject);
		return hProject;
	}
}
