/*******************************************************************************
 * Copyright (c) 2013, 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.plugin.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.thym.core.internal.cordova.CordovaCLIErrors;
import org.eclipse.thym.core.internal.util.FileUtils;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.core.plugin.CordovaPlugin;
import org.eclipse.thym.core.plugin.CordovaPluginManager;
import org.eclipse.thym.hybrid.test.Activator;
import org.eclipse.thym.hybrid.test.RequiresCordovaCLICategory;
import org.eclipse.thym.hybrid.test.TestProject;
import org.eclipse.thym.hybrid.test.TestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@SuppressWarnings("restriction")
public class PluginInstallationTests {
	
	private static File pluginsDirectroy;
	private TestProject project;
	private final static String PLUGIN_DIR_TESTPLUGIN = "testPlugin";
	private final static String PLUGIN_DIR_NAMESPACEPLUGIN = "NamespacePlugin";
	private final static String PLUGIN_ID_TESTPLUGIN = "org.eclipse.thym.test";
	private final static String PLUGIN_ID_NAMESPACEPLUGIN = "org.eclipse.thym.test.namespace";
	private final static String PLUGIN_DIR_VARIABLE = "VariablePlugin";
	
	@BeforeClass
	public static void setUpPlugins() throws IOException{
		URL pluginsDir = Activator.getDefault().getBundle().getEntry("/plugins");
		File tempDir =TestUtils.getTempDirectory();
		pluginsDirectroy = new File(tempDir, "plugins");
		FileUtils.directoryCopy(pluginsDir, FileUtils.toURL(pluginsDirectroy));
		
	}
	
	@AfterClass
	public static void cleanUpPlugins() throws IOException{
		if(pluginsDirectroy != null )
		{
			org.apache.commons.io.FileUtils.forceDelete(pluginsDirectroy);
		}
	}
	
	@Before
	public void setUpTestProject(){
		project = new TestProject();
	}
	
	@After
	public void cleanProject() {
		
		try {
			if(this.project != null ){
				this.project.delete();
				this.project = null;
			}
		} catch (CoreException err) {
			fail(err.getMessage());
		}
	}

	private CordovaPluginManager getCordovaPluginManager() {
		CordovaPluginManager pm = project.hybridProject().getPluginManager();		
		assertNotNull(pm);
		return pm;
	}
	
	@Test
	@Category(value=RequiresCordovaCLICategory.class)
	public void installPluginTest() throws CoreException{
		installPlugin(PLUGIN_DIR_TESTPLUGIN);
		IProject prj = project.getProject();
		IFolder plgFolder = prj.getFolder("/"+PlatformConstants.DIR_PLUGINS+"/"+PLUGIN_ID_TESTPLUGIN);
		assertNotNull(plgFolder);
		assertTrue(plgFolder.exists());
	}
	
	@Test
	@Category(value=RequiresCordovaCLICategory.class)
	public void checkFetchJson() throws CoreException{
		installPlugin(PLUGIN_DIR_TESTPLUGIN);
		IProject prj = project.getProject();
		IFolder plgFolder = prj.getFolder("/"+PlatformConstants.DIR_PLUGINS);
		assertNotNull(plgFolder);
		IFile fetchJson = plgFolder.getFile("fetch.json");
		assertNotNull(fetchJson);
		assertTrue(fetchJson.exists());
	}
	
	@Test
	@Category(value=RequiresCordovaCLICategory.class)
	public void installPluginNamespace() throws CoreException{
		installPlugin(PLUGIN_DIR_NAMESPACEPLUGIN);
		IProject prj = project.getProject();
		IFolder plgFolder = prj.getFolder("/"+PlatformConstants.DIR_PLUGINS+"/"+PLUGIN_ID_NAMESPACEPLUGIN);
		assertNotNull(plgFolder);
		assertTrue(plgFolder.exists());
	}
	
	@Test
	@Category(value=RequiresCordovaCLICategory.class)
	public void installVariablePluginTest(){
		try{
			installPlugin(PLUGIN_DIR_VARIABLE);
		}catch(CoreException e){
			IStatus status = e.getStatus();
			assertNotNull(status);
			assertEquals(CordovaCLIErrors.ERROR_MISSING_PLUGIN_VARIABLE, status.getCode());
			return;
		}
		fail("No CoreException generated");
	}
	
	@Test
	public void listNoPluginsTest() throws CoreException{
		CordovaPluginManager pm = getCordovaPluginManager();
		List<CordovaPlugin> plugins = pm.getInstalledPlugins();
		assertTrue(plugins.toString(),plugins.isEmpty());
	}
	
	@Test
	@Category(value=RequiresCordovaCLICategory.class)
	public void listPluginsTest() throws CoreException{
	
		CordovaPluginManager pm =installPlugin(PLUGIN_DIR_TESTPLUGIN);
		List<CordovaPlugin> plugins = pm.getInstalledPlugins();
		boolean found = false;
		for (CordovaPlugin cordovaPlugin : plugins) {
			if(PLUGIN_ID_TESTPLUGIN.equals(cordovaPlugin.getId())){
				found = true;
			}
		}
		assertTrue("installed plugin not listed",found);
		assertTrue(pm.isPluginInstalled(PLUGIN_ID_TESTPLUGIN));
	}
	
	@Test
	@Category(value=RequiresCordovaCLICategory.class)
	public void pluginNotInstalledTest() throws CoreException{
		CordovaPluginManager pm = installPlugin(PLUGIN_DIR_TESTPLUGIN);
		assertFalse(pm.isPluginInstalled("my.madeup.id"));
		assertTrue(pm.isPluginInstalled(PLUGIN_ID_TESTPLUGIN));
	}

	@Test
	@Category(value=RequiresCordovaCLICategory.class)
	public void installPluginToProjectWithoutPluginsFolder() throws CoreException{
		IProject prj = project.getProject();
		IFolder pluginsFolder  = prj.getFolder(PlatformConstants.DIR_PLUGINS);
		assertNotNull(pluginsFolder);
		assertTrue(pluginsFolder.exists());
		pluginsFolder.delete( true, new NullProgressMonitor());
		assertFalse(pluginsFolder.exists());
		installPlugin(PLUGIN_DIR_TESTPLUGIN);
		IFolder plgFolder = prj.getFolder("/"+PlatformConstants.DIR_PLUGINS+"/"+PLUGIN_ID_TESTPLUGIN);
		assertNotNull(plgFolder);
		assertTrue(plgFolder.exists());
	}
	
	@Test
	@Category(value=RequiresCordovaCLICategory.class)
	public void installPluginFromGit() throws CoreException{
		CordovaPluginManager pm = getCordovaPluginManager();
		URI uri = URI.create("https://github.com/apache/cordova-plugin-console.git#r0.2.0");
		pm.installPlugin(uri, new NullProgressMonitor());
		List<CordovaPlugin> plugins = pm.getInstalledPlugins();
		boolean found = false;
		for (CordovaPlugin cordovaPlugin : plugins) {
			if("org.apache.cordova.core.console".equals(cordovaPlugin.getId())){
				assertFalse(found);
				found =true;
				assertEquals("0.2.0",cordovaPlugin.getVersion());
				break;
			}
		}
		assertTrue("git installed plugin not found",found);
		
	}
	

	private CordovaPluginManager installPlugin(String pluginsSubdir) throws CoreException {
		final CordovaPluginManager pm = getCordovaPluginManager();
		final File directory = new File(pluginsDirectroy, pluginsSubdir);
		assertTrue(pluginsSubdir+ " does not exist", directory.exists());
		Job installJob = new Job("Install plugin") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					pm.installPlugin(directory, new NullProgressMonitor());
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		IWorkspace ws= ResourcesPlugin.getWorkspace();
		ISchedulingRule rule = ws.getRuleFactory().modifyRule(project.getProject());
		
		installJob.setRule(rule);
		installJob.schedule();
		
		try {
			installJob.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
		if(installJob.getResult() != Status.OK_STATUS){
			if(installJob.getResult().getException() != null){
				installJob.getResult().getException().printStackTrace();
			}
			throw new CoreException(installJob.getResult());
		}
		
		return pm;
	}

}
