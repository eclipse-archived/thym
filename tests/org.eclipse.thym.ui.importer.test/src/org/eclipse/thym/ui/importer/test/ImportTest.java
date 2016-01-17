/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 	- Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.thym.ui.importer.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.taskdefs.Expand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.internal.ui.importer.CordovaProjectConfigurator;
import org.eclipse.ui.internal.wizards.datatransfer.EasymportJob;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class ImportTest {

	@Test
	public void test() throws Exception {
		ReadableByteChannel channel = null;
		try {
			channel = Channels.newChannel(new URL("https://github.com/apache/cordova-app-hello-world/archive/master.zip").openStream()); //$NON-NLS-1$
		} catch (IOException ex) {
			Assume.assumeNoException("This test require ability to connect to Internet", ex); //$NON-NLS-1$
		}
		File outputFile = File.createTempFile("cordova-app-hello-world", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
		FileOutputStream fos = new FileOutputStream(outputFile);
		fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
		channel.close();
		fos.close();
		Expand expand = new Expand();
		expand.setSrc(outputFile);
		File outputDirectory = Files.createTempDirectory("cordova-app-hello-world").toFile(); //$NON-NLS-1$
		expand.setDest(outputDirectory);
		expand.execute();
		outputFile.delete();

		Set<IProject> newProjects = null;
		EasymportJob job = new EasymportJob(outputDirectory, Collections.EMPTY_SET, true, true);
		try {
			Map<File, List<ProjectConfigurator>> proposals = job.getImportProposals(new NullProgressMonitor());
			Assert.assertEquals("Expected only 1 project to import", 1, proposals.size()); //$NON-NLS-1$
			boolean thymConfiguratorFound = false;
			for (ProjectConfigurator configurator : proposals.values().iterator().next()) {
				if (configurator instanceof CordovaProjectConfigurator) {
					thymConfiguratorFound = true;
				}
			}
			Assert.assertTrue("Cordova configurator not found while checking directory", thymConfiguratorFound); //$NON-NLS-1$
			
			// accept proposals
			job.setDirectoriesToImport(proposals.keySet());
	
			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			Set<IProject> beforeImport = new HashSet<>(Arrays.asList(wsRoot.getProjects()));
			job.run(new NullProgressMonitor());
			job.join();
			newProjects = new HashSet<>(Arrays.asList(wsRoot.getProjects()));
			newProjects.removeAll(beforeImport);
			Assert.assertEquals("Expected only 1 new project", 1, newProjects.size()); //$NON-NLS-1$
			IProject newProject = newProjects.iterator().next();
			boolean startsWith = newProject.getLocation().toFile().getAbsolutePath().startsWith(outputDirectory.toPath().toRealPath().toAbsolutePath().toString());
			Assert.assertTrue(startsWith);
			HybridProject hybridProject = HybridProject.getHybridProject(newProject);
			Assert.assertNotNull("Project not configured as hybrid", hybridProject); //$NON-NLS-1$
		} finally {
			if (newProjects != null) {
				for (IProject project : newProjects) {
					project.delete(true, true, new NullProgressMonitor());
				}
			}
		}
	}

}