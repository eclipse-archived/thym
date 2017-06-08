/*******************************************************************************
 * Copyright (c) 2015, 2017 Red Hat, Inc. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat Inc. - initial API and implementation and/or initial
 * documentation
 *******************************************************************************/
package org.eclipse.thym.core.test;
import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.thym.core.config.Engine;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.HybridMobileEngineManager;
import org.eclipse.thym.core.engine.internal.cordova.CordovaEngineProvider;
import org.eclipse.thym.hybrid.test.TestProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class HybridMobileEngineTests {
	
	private TestProject testproject;
	private TestProject testProjectWithoutEngine;
	private HybridMobileEngineManager manager;
	private HybridMobileEngineManager managerWithoutEngine;
	private static CordovaEngineProvider provider = CordovaEngineProvider.getInstance();
	private HybridMobileEngine testEngine;
	
	public static final String PROJECT_NAME1 = "HybridToolsTest1";
	public static final String APPLICATION_NAME1 = "Test applciation1";
	public static final String APPLICATION_ID1 = "hybrid.tools.test1";

	@Before 
	public void setUpHybridMobileManager() throws CoreException{
		testEngine = provider.createEngine("android", "1.0.0");
		// CordovaEngineProvider.engineFound() just places the provided engine in the
		// static installed engines list.
		provider.engineFound(testEngine);
		
		testproject = new TestProject();
		manager = new HybridMobileEngineManager(testproject.hybridProject());
		// To support testing on platforms that don't have any cordova engines installed,
		// we need to create a temporary engine and trick CordovaEngineProvider into
		// thinking it's installed.
		
		testProjectWithoutEngine = new TestProject(false, PROJECT_NAME1, APPLICATION_NAME1, APPLICATION_ID1);
		managerWithoutEngine = new HybridMobileEngineManager(testProjectWithoutEngine.hybridProject());
	}
	
	@After
	public void cleanUpHybridMobileManager() throws CoreException{
		if(testproject != null ){
			testproject.delete();
			testproject=null;
		}
		if(manager != null ){
			manager = null;
		}
		if(testProjectWithoutEngine != null){
			testProjectWithoutEngine.delete();
			testProjectWithoutEngine = null;
		}
		// deleteEngineLibraries() has the side effect of clearing the engineList in
		// CordovaEngineProvider (this is also why testEngine needs to have a path set).
		provider.deleteEngine(testEngine);
	}
	
	@Test
	public void testHybridMobileManagerActiveEngines() throws CoreException{
		//Test project is created with default engines so we expect them to be equal.
		List<HybridMobileEngine> defaultEngines = provider.defaultEngines();
		assertArrayEquals(defaultEngines.toArray(new HybridMobileEngine[defaultEngines.size()]), manager.getEngines());
		
		//Project has no engine
		assertTrue(managerWithoutEngine.getEngines().length == 0);
	}
	
	@Test
	public void testHybridMobileManagerUpdateEngines() throws CoreException{
		final HybridMobileEngine[] engines = new HybridMobileEngine[2];
		engines[0] = new HybridMobileEngine("platform_0", "0.0.0", null); 
		engines[1] = new HybridMobileEngine("platform_1","1.1.1", null);
		manager.updateEngines(engines);
		//Run on a IWorkspaceRunnable because it needs to sync with the udpateEngines call.
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				Widget w = WidgetModel.getModel(testproject.hybridProject()).getWidgetForRead();
				assertEquals("Before Update - ", engines.length, w.getEngines().size());
				checkEnginesPersistedCorrectly(engines);
				manager.updateEngines(engines);
				w = WidgetModel.getModel(testproject.hybridProject()).getWidgetForRead();
				assertEquals("After Update - ", engines.length, w.getEngines().size());
				checkEnginesPersistedCorrectly(engines);
			}
		};
		IWorkspace ws= ResourcesPlugin.getWorkspace();
		ISchedulingRule rule = ws.getRuleFactory().modifyRule(testproject.getProject());
		ws.run(runnable, rule, 0,new NullProgressMonitor());
	}

	//Check given set of engines are persisted to config.xml correctly
	private void checkEnginesPersistedCorrectly(final HybridMobileEngine[] engines) throws CoreException {
		Widget w = WidgetModel.getModel(testproject.hybridProject()).getWidgetForRead();
		assertEquals(engines.length, w.getEngines().size());
		List<Engine> persistedEngines = w.getEngines();
		for (HybridMobileEngine hybridMobileEngine : engines) {
			boolean enginePersisted =false;
			for (Engine engine : persistedEngines) {
				if(hybridMobileEngine.getName().equals(engine.getName()) &&
						hybridMobileEngine.getSpec().equals(engine.getSpec())){
					enginePersisted= true;
					break;
				}
			}
			assertTrue("HybridMobile Engine is not persisted correctly",enginePersisted);
		}
	}
	
	@Test
	public void testHybridMobileEngineEquals(){
		HybridMobileEngine engine_0 = new HybridMobileEngine("platform_0", "0.0.0",null); 
		HybridMobileEngine engine_1 = new HybridMobileEngine("platform_0", "0.0.0",null);  
		assertEquals(engine_0, engine_1);
	}

	@Test
	public void testManagerGetActiveFromJsonNoFile() throws CoreException {
		testproject.deletePlatformsJson();
		HybridMobileEngine[] engines = manager.getEnginesFromPlatformsJson();
		assertTrue("GetActiveEnginesFromPlatformsJson() should return empty array if platforms.json does not exist",
				engines.length == 0);
	}

	@Test
	public void testManagerGetActiveFromJsonInvalidJson() throws CoreException {
		// Currently, a malformed platforms.json file is simply ignored.
		testproject.writePlatformsJson("{ android: ");
		HybridMobileEngine[] engines = manager.getEnginesFromPlatformsJson();
		assertTrue(engines.length == 0);
	}
	
	@Test
	public void testManagerHasEngines(){
		assertFalse(managerWithoutEngine.hasActiveEngine());
		assertTrue(manager.hasActiveEngine());
	}

	@Test
	public void testManagerGetActiveFromJsonOnePlatform() throws CoreException {
		testproject.writePlatformsJson("{ \"android\" : \"/temporary/test/location\" }");
		HybridMobileEngine[] engines = manager.getEnginesFromPlatformsJson();
		assertTrue("Returned array should contain one engine.", engines.length == 1);
		assertEquals(engines[0].getName(), "android");
		assertEquals(engines[0].getSpec(), "/temporary/test/location");
	}
	
	@Test
	public void testManagerGetActiveFromJsonNoPlatform() throws CoreException {
		testProjectWithoutEngine.writePlatformsJson("{}");
		HybridMobileEngine[] engines = managerWithoutEngine.getEnginesFromPlatformsJson();
		assertTrue(engines.length == 0);
	}

	@Test
	public void testManagerGetActiveFromJsonWithUnknownEngine() throws CoreException {
		testproject.writePlatformsJson(
				"{ \"android\" : \"/temporary/test/location\", "
				+ "\"unknown\" : \"3.8.0\" }");
		HybridMobileEngine[] engines = manager.getEnginesFromPlatformsJson();
		assertTrue(engines.length == 2);
		assertEquals(engines[0].getName(), "android");
		assertEquals(engines[0].getSpec(), "/temporary/test/location");
		assertEquals(engines[1].getName(), "unknown");
		assertEquals(engines[1].getSpec(), "3.8.0");
	}
}
