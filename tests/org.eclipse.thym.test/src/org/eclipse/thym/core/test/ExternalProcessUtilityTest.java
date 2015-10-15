/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.test;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.thym.core.internal.util.ExternalProcessUtility;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("restriction")
public class ExternalProcessUtilityTest {
	
	@Test(expected= IllegalArgumentException.class)
	public void testInvalidCommands_1() throws CoreException{
		ExternalProcessUtility epu = new ExternalProcessUtility();
		epu.execAsync((String)null, null, null, null, null);
	}

	@Test(expected= IllegalArgumentException.class)
	public void testInvalidCommands_2() throws CoreException{
		ExternalProcessUtility epu = new ExternalProcessUtility();
		epu.execAsync((String[])null, null, null, null, null);
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void testInvalidCommands_3() throws CoreException{
		ExternalProcessUtility epu = new ExternalProcessUtility();
		epu.execSync((String)null, null, null, null, null, null,null);
	}

	@Test(expected= IllegalArgumentException.class)
	public void testInvalidCommands_4() throws CoreException{
		ExternalProcessUtility epu = new ExternalProcessUtility();
		epu.execSync((String[])null, null, null, null, null, null,null);
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void testInvalidDirectorySync() throws CoreException{
		ExternalProcessUtility epu = new ExternalProcessUtility();
		epu.execSync("java -version", new File("someDirName"), null, null, null, null,null);
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void testInvalidDirectoryAsync() throws CoreException{
		ExternalProcessUtility epu = new ExternalProcessUtility();
		epu.execAsync("java -version", new File("someDirName"), null, null, null);
	}
	
	@Test
	public void testRunJavaVersionSync() throws CoreException{
		ExternalProcessUtility epu = new ExternalProcessUtility();
		epu.execSync("java -version", null, null, null, null, null,null);
	}
		
	@Test
	public void testRunJavaVersionSync_CmdArray() throws CoreException{
		ExternalProcessUtility epu = new ExternalProcessUtility();
		int i = epu.execSync(new String[] {"java","-version"}, null, null, null, null, null,null);
		Assert.assertEquals("java version call failed", 0, i);
	}

}
