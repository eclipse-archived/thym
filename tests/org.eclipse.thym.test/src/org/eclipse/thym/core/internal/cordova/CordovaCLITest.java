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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy2;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.engine.HybridMobileEngineManager;
import org.eclipse.thym.core.internal.cordova.CordovaCLI.Command;
import org.eclipse.thym.ui.wizard.project.HybridProjectCreator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("restriction")
public class CordovaCLITest {
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
    	CordovaCLI.newCLIforProject(null);
    }
    
    @Test
    public void testAdditionalEnvProperties() throws CoreException{
    	CordovaCLI mockCLI = getMockCLI();
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
    	CordovaCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);
    	mockCLI.prepare(new NullProgressMonitor(), "android");
    	verify(mockStreams).write("cordova prepare android\n");
    }
    
    @Test
    public void testGeneratedPluginCommandCorrectlyForADD() throws CoreException,IOException{
    	CordovaCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.plugin(Command.ADD, new NullProgressMonitor(), "cordova-plugin-console@1.0.1", CordovaCLI.OPTION_SAVE);
    	verify(mockStreams).write("cordova plugin add cordova-plugin-console@1.0.1 --save\n");
    }
    
    @Test
    public void testGeneratedPluginCommandCorrectlyForREMOVE() throws CoreException,IOException{
    	CordovaCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.plugin(Command.REMOVE, new NullProgressMonitor(), "cordova-plugin-console", CordovaCLI.OPTION_SAVE);
    	verify(mockStreams).write("cordova plugin remove cordova-plugin-console --save\n");
    }
    
    @Test
    public void testGeneratedPlatformCommandCorrectlyForADD() throws CoreException,IOException{
    	CordovaCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.platform(Command.ADD, new NullProgressMonitor(), "ios@5.0.0", CordovaCLI.OPTION_SAVE);
    	verify(mockStreams).write("cordova platform add ios@5.0.0 --save\n");
    }
    
    @Test
    public void testGeneratedPlatformCommandCorrectlyForREMOVE() throws CoreException,IOException{
    	CordovaCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.platform(Command.REMOVE, new NullProgressMonitor(), "ios", CordovaCLI.OPTION_SAVE);
    	verify(mockStreams).write("cordova platform remove ios --save\n");
    }    

    @Test
    public void testGeneratedBuildCommandCorrectly() throws CoreException,IOException{
    	CordovaCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.build(new NullProgressMonitor(), "ios");
    	verify(mockStreams).write("cordova build ios\n");
    }
    
    @Test
    public void testGeneratedVersionCommandCorrectly() throws CoreException,IOException{
    	CordovaCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.version(new NullProgressMonitor());
    	verify(mockStreams).write("cordova -version\n");
    } 
    
    @Test
    public void testGeneratedNodeVersionCommandCorrectly() throws CoreException,IOException{
    	CordovaCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);   	
    	mockCLI.nodeVersion(new NullProgressMonitor());
    	verify(mockStreams).write("node -v\n");
    } 
    
    @Test
    public void testCordovaCLIResult(){
    	String resultText = "Failed to fetch platform ios@7.0.1 \n"+
    			"Probably this is either a connection problem, or platform spec is incorrect. \n"+
    			"Check your connection and platform name/version/URL. \n"+
    			"Error: version not found: cordova-ios@7.0.1 \n";    	
    	CordovaCLIResult result = new CordovaCLIResult(resultText);
    	assertEquals(IStatus.OK, result.asStatus().getSeverity());
    	assertEquals(resultText, result.getMessage());
    }
    
    @Test
    public void testErrorDetectingCLIResult(){
    	String resultText = "Failed to fetch platform ios@7.0.1 \n"+
    			"Probably this is either a connection problem, or platform spec is incorrect.\n"+
    			"Check your connection and platform name/version/URL.\n"+
    			"Error: version not found: cordova-ios@7.0.1 \n";
    			
    	CordovaCLIResult result = new CordovaCLIResult(resultText);
    	ErrorDetectingCLIResult err = result.convertTo(ErrorDetectingCLIResult.class);
    	IStatus status = err.asStatus();
    	assertEquals("expected error status but failed",IStatus.ERROR,status.getSeverity());
    	assertEquals(CordovaCLIErrors.ERROR_GENERAL, status.getCode());
    	assertEquals("version not found: cordova-ios@7.0.1", status.getMessage());
    }
    
    @Test
    public void testErrorDetectingCLIResult_missingCommand_WIN(){
    	String resultText = "'cordova' is not recognized as an internal or external command,\n"+
    						"operable program or batch file.";
    			
    	CordovaCLIResult result = new CordovaCLIResult(resultText);
    	ErrorDetectingCLIResult err = result.convertTo(ErrorDetectingCLIResult.class);
    	IStatus status = err.asStatus();
    	assertEquals("expected error status but failed",IStatus.ERROR,status.getSeverity());
    	assertEquals(CordovaCLIErrors.ERROR_COMMAND_MISSING, status.getCode());
    }
    
    @Test
    public void testErrorDetectingCLIResult_missingCommand_MAC(){
    	String resultText = "-bash: cordova: command not found";
    	
    	CordovaCLIResult result = new CordovaCLIResult(resultText);
    	ErrorDetectingCLIResult err = result.convertTo(ErrorDetectingCLIResult.class);
    	IStatus status = err.asStatus();
    	assertEquals("expected error status but failed",IStatus.ERROR,status.getSeverity());
    	assertEquals(CordovaCLIErrors.ERROR_COMMAND_MISSING, status.getCode());
    }   
    
	private void setupMocks(CordovaCLI mockCLI, IProcess mockProcess, IStreamsProxy2 mockStreams) throws CoreException {
		when(mockCLI.startShell(any(IStreamListener.class), any(IProgressMonitor.class),any(ILaunchConfiguration.class))).thenReturn(mockProcess);
		doReturn(mockStreams).when(mockProcess).getStreamsProxy();
		doReturn(Boolean.TRUE).when(mockProcess).isTerminated();
	}

	private CordovaCLI getMockCLI() {
		CordovaCLI mockCLI = spy(CordovaCLI.newCLIforProject(getTheProject()));
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
