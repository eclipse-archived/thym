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
package org.eclipse.thym.ui.test.platforms;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.internal.cordova.CordovaEngineProvider;
import org.eclipse.thym.core.internal.cordova.CordovaCLIResult;
import org.eclipse.thym.core.internal.cordova.CordovaProjectCLI;
import org.eclipse.thym.core.internal.cordova.CordovaProjectCLI.Command;
import org.eclipse.thym.hybrid.test.TestProject;
import org.eclipse.thym.ui.platforms.internal.PlatformRemovalAction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;

public class PlatformRemovalActionTest {
	
	static TestProject project;
	CordovaProjectCLI cliMock;
	
	
	@BeforeClass
	public static void setUpTestProject(){
		project = new TestProject();
	}
	
	@AfterClass 
	public static void deleteTestProject() throws CoreException{
		if(project != null) {
			project.delete();
		}
	}
	
	@Test
	public void testAction() throws CoreException {
		assertTrue(project.hybridProject().getEngineManager().getEngines().length > 0);
		List<HybridMobileEngine> enginesToRemove = CordovaEngineProvider.getInstance().defaultEngines();
		assertTrue(enginesToRemove.size() > 1);
		PlatformRemovalAction act = new PlatformRemovalAction(getMockProject(), enginesToRemove);
		act.run();
		for(HybridMobileEngine eng: enginesToRemove) {
			verify(cliMock).platform(eq(Command.REMOVE), any(IProgressMonitor.class), eq(eng.getName()), any(String.class));
		}
	}
	
	private HybridProject getMockProject() throws CoreException {
		HybridProject mockProject = spy(project.hybridProject());
		cliMock = mock(CordovaProjectCLI.class);
		doReturn(cliMock).when(mockProject).getProjectCLI();
		doReturn(new CordovaCLIResult("")).when(cliMock).platform(eq(Command.REMOVE), any(IProgressMonitor.class), Matchers.<String>anyVararg());
		return mockProject;
	}

}
