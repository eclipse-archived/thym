/*******************************************************************************
 * Copyright (c) 2015, 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 * 		 IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.thym.core.internal.cordova;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy2;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class CordovaCLITest {
	
	@Test
    public void testAdditionalEnvProperties() throws CoreException{
		CordovaCLI mockCLI = getMockCLI();
    	IProcess mockProcess = mock(IProcess.class);
    	IStreamsProxy2 mockStreams  = mock(IStreamsProxy2.class);
    	setupMocks(mockCLI, mockProcess, mockStreams);
    	
    	ArgumentCaptor<ILaunchConfiguration> confCaptor = ArgumentCaptor.forClass(ILaunchConfiguration.class);
    	mockCLI.version(new NullProgressMonitor());
    	verify(mockCLI).startShell(any(IStreamListener.class), any(IProgressMonitor.class), confCaptor.capture());
    	Map<String,String> attr = confCaptor.getValue().getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map<String,String>)null);
    	assertEquals(EnvironmentPropsExt.ENV_VALUE, attr.get(EnvironmentPropsExt.ENV_KEY));
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
	
	private CordovaCLI getMockCLI() {
		CordovaCLI mockCLI = spy(new CordovaCLI());
		return mockCLI;
	}
	
	private void setupMocks(CordovaCLI mockCLI, IProcess mockProcess, IStreamsProxy2 mockStreams) throws CoreException {
		doReturn(mockProcess).when(mockCLI).startShell(any(IStreamListener.class), any(IProgressMonitor.class),any(ILaunchConfiguration.class));
		when(mockProcess.getStreamsProxy()).thenReturn(mockStreams);
		when(mockProcess.isTerminated()).thenReturn(Boolean.TRUE);
	}

}
