/*******************************************************************************
 * Copyright (c) 2013, 2014 Red Hat, Inc. 
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.thym.core.config.Preference;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.core.internal.util.FileUtils;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.core.plugin.CordovaPlugin;
import org.eclipse.thym.core.plugin.CordovaPluginManager;
import org.eclipse.thym.core.plugin.FileOverwriteCallback;
import org.eclipse.thym.core.plugin.RestorableCordovaPlugin;
import org.eclipse.thym.hybrid.test.Activator;
import org.eclipse.thym.hybrid.test.TestProject;
import org.eclipse.thym.hybrid.test.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@SuppressWarnings("restriction")
public class PluginInstallationTests {
	
	private static File pluginsDirectroy;
	private TestProject project;
	private final static String PLUGIN_DIR_TESTPLUGIN = "testPlugin";
	private final static String PLUGIN_DIR_NAMESPACEPLUGIN = "NamespacePlugin";
	private final static String PLUGIN_ID_TESTPLUGIN = "org.eclipse.thym.test";
	private final static String PLUGIN_ID_NAMESPACEPLUGIN = "org.eclipse.thym.test.namespace";
	private final static String PLUGIN_DIR_VARIABLE = "VariablePlugin";
	private final static String PLUGIN_ID_VARIABLE = "org.eclipse.variable";
	
	@BeforeClass
	public static void setUpPlugins() throws IOException{
		URL pluginsDir = Activator.getDefault().getBundle().getEntry("/plugins");
		File tempDir =TestUtils.getTempDirectory();
		pluginsDirectroy = new File(tempDir, "plugins");
		FileUtils.directoryCopy(pluginsDir, FileUtils.toURL(pluginsDirectroy));
		
	}
	
	@Before
	public void setUpTestProject(){
		project = new TestProject();
	}
	
	@After
	public void cleanProject() throws CoreException{
		if(this.project != null ){
			this.project.delete();
			this.project = null;
		}
	}

	private CordovaPluginManager getCordovaPluginManager() {
		CordovaPluginManager pm = project.hybridProject().getPluginManager();		
		assertNotNull(pm);
		return pm;
	}
	
	@Test
	public void installPluginTest() throws CoreException{
		installPlugin(PLUGIN_DIR_TESTPLUGIN);
		IProject prj = project.getProject();
		IFolder plgFolder = prj.getFolder("/"+PlatformConstants.DIR_PLUGINS+"/"+PLUGIN_ID_TESTPLUGIN);
		assertNotNull(plgFolder);
		assertTrue(plgFolder.exists());
	}
	
	@Test
	public void checkFetchJson() throws CoreException{
		installPlugin(PLUGIN_DIR_TESTPLUGIN);
		IProject prj = project.getProject();
		IFolder plgFolder = prj.getFolder("/"+PlatformConstants.DIR_PLUGINS+"/"+PLUGIN_ID_TESTPLUGIN);
		assertNotNull(plgFolder);
		IFile fetchJson = plgFolder.getFile(".fetch.json");
		assertNotNull(fetchJson);
		assertTrue(fetchJson.exists());
		JsonParser parser = new JsonParser();
		InputStreamReader reader = new InputStreamReader(fetchJson.getContents());
		JsonElement element = parser.parse(reader);
		JsonObject object = element.getAsJsonObject();
		assertTrue(object.has("source"));
		JsonObject source = object.getAsJsonObject("source");
		assertTrue(source.has("type"));
		assertTrue(source.has("path"));
		String type = source.get("type").getAsString();
		assertEquals("local",type);
	}
	
	@Test
	public void installPluginNamespace() throws CoreException{
		installPlugin(PLUGIN_DIR_NAMESPACEPLUGIN);
		IProject prj = project.getProject();
		IFolder plgFolder = prj.getFolder("/"+PlatformConstants.DIR_PLUGINS+"/"+PLUGIN_ID_NAMESPACEPLUGIN);
		assertNotNull(plgFolder);
		assertTrue(plgFolder.exists());
	}
	
	
	@Test
	public void installVariablePluginTest() throws CoreException{
		installPlugin(PLUGIN_DIR_VARIABLE);
		IProject prj = project.getProject();
		IFolder plgFolder = prj.getFolder("/plugins/"+PLUGIN_ID_VARIABLE);
		assertNotNull(plgFolder);
		assertTrue(plgFolder.exists());
		WidgetModel model = WidgetModel.getModel(project.hybridProject());
		Widget widget = model.getWidgetForRead();
		List<Preference> prefs = widget.getPreferences();
		for (Preference preference : prefs) {
			if(preference.getName().equals("API_KEY")){
				return;
			}
		}
		fail("Replaced key is not found");
	}
	
	@Test
	public void listNoPluginsTest() throws CoreException{
		CordovaPluginManager pm = getCordovaPluginManager();
		List<CordovaPlugin> plugins = pm.getInstalledPlugins();
		assertTrue(plugins.isEmpty());
	}
	
	@Test
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
	public void pluginNotInstalledTest() throws CoreException{
		CordovaPluginManager pm = installPlugin(PLUGIN_DIR_TESTPLUGIN);
		assertFalse(pm.isPluginInstalled("my.madeup.id"));
		assertTrue(pm.isPluginInstalled(PLUGIN_ID_TESTPLUGIN));
	}

	@Test
	public void restorablePluginListTest() throws CoreException{
		CordovaPluginManager pm = getCordovaPluginManager();
		List<RestorableCordovaPlugin> restorables = pm.getRestorablePlugins(new NullProgressMonitor());
		assertTrue("config.xml already contains restorables",restorables.isEmpty());
		pm = installPlugin(PLUGIN_DIR_TESTPLUGIN);
 		restorables = pm.getRestorablePlugins(new NullProgressMonitor());
		assertNotNull( restorables);
		for (RestorableCordovaPlugin plugin : restorables) {
			assertNotEquals(plugin.getId(),PLUGIN_ID_TESTPLUGIN);
		}
	}
	
	@Test
	public void installPluginToProjectWithoutPluginsFolder() throws CoreException{
		System.out.println("running installPluginToProjectWithoutPluginsFolder");
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
	
	//Disabled for now to resolve HIPP internet access issues
	public void installPluginFromGit() throws CoreException{
		CordovaPluginManager pm = getCordovaPluginManager();
		URI uri = URI.create("https://github.com/apache/cordova-plugin-console.git#r0.2.0");
		pm.installPlugin(uri, new FileOverwriteCallback() {
				
			@Override
			public boolean isOverwiteAllowed(String[] files) {
				return true;
			}
		}, false, new NullProgressMonitor());
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
		CordovaPluginManager pm = getCordovaPluginManager();
		File directory = new File(pluginsDirectroy, pluginsSubdir);
		assertTrue(pluginsSubdir+ " does not exist", directory.exists());
		pm.installPlugin(directory,new FileOverwriteCallback() {
			
			@Override
			public boolean isOverwiteAllowed(String[] files) {
				return true;
			}
		}, new NullProgressMonitor());
		return pm;
	}
	

}
