/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. All rights reserved. This program and
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
import org.eclipse.core.runtime.Path;
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
	private HybridMobileEngineManager manager;

	@Before 
	public void setUpHybridMobileManager() throws CoreException{
		testproject = new TestProject();
		manager = new HybridMobileEngineManager(testproject.hybridProject());
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
	}
	
	@Test
	public void testHybridMobileManagerActiveEngines() throws CoreException{
		//Test project is created with default engines so we expect them to be equal.
		assertArrayEquals(HybridMobileEngineManager.defaultEngines(), manager.getActiveEngines());
	}
	
	@Test
	public void testHybridMobileManagerUpdateEngines() throws CoreException{
		final HybridMobileEngine[] engines = new HybridMobileEngine[2];
		engines[0] = new HybridMobileEngine(); 
		engines[0].setId("platform_0");
		engines[0].setVersion("0.0.0");
		engines[1] = new HybridMobileEngine();
		engines[1].setId("platform_1");
		engines[1].setVersion("1.1.1");
		manager.updateEngines(engines);
		//Run on a IWorkspaceRunnable because it needs to sync with the udpateEngines call.
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				Widget w = WidgetModel.getModel(testproject.hybridProject()).getWidgetForRead();
				assertEquals(engines.length, w.getEngines().size());
				checkEnginesPersistedCorrectly(engines);
				manager.updateEngines(engines);
				w = WidgetModel.getModel(testproject.hybridProject()).getWidgetForRead();
				assertEquals(engines.length, w.getEngines().size());
				checkEnginesPersistedCorrectly(engines);
			}
		};
		IWorkspace ws= ResourcesPlugin.getWorkspace();
		ISchedulingRule rule = ws.getRuleFactory().modifyRule(testproject.getProject());
		ResourcesPlugin.getWorkspace().run(runnable, rule, 0,new NullProgressMonitor());
	}

	//Check given set of engines are persisted to config.xml correctly
	private void checkEnginesPersistedCorrectly(final HybridMobileEngine[] engines) throws CoreException {
		Widget w = WidgetModel.getModel(testproject.hybridProject()).getWidgetForRead();
		assertEquals(engines.length, w.getEngines().size());
		List<Engine> persistedEngines = w.getEngines();
		for (HybridMobileEngine hybridMobileEngine : engines) {
			boolean enginePersisted =false;
			for (Engine engine : persistedEngines) {
				if(hybridMobileEngine.getId().equals(engine.getName()) &&
						hybridMobileEngine.getVersion().equals(engine.getSpec())){
					enginePersisted= true;
					break;
				}
			}
			assertTrue("HybridMobile Engine is not persisted correctly",enginePersisted);
		}
	}
	
	@Test
	public void testHybridMobileEngineEquals(){
		HybridMobileEngine engine_0 = new HybridMobileEngine(); 
		engine_0.setId("platform_0");
		engine_0.setVersion("0.0.0");
		HybridMobileEngine engine_1 = new HybridMobileEngine(); 
		engine_1.setId("platform_0");
		engine_1.setVersion("0.0.0");
		assertEquals(engine_0, engine_1);
	}
	
	@Test
	public void testHybridMobileEngineIsManaged(){
		HybridMobileEngine engine_0 = new HybridMobileEngine(); 
		engine_0.setId("platform_0");
		engine_0.setVersion("0.0.0");
		engine_0.setLocation(CordovaEngineProvider.getLibFolder().append("myplatform"));
		assertTrue(engine_0.isManaged());
		HybridMobileEngine engine_1 = new HybridMobileEngine(); 
		engine_1.setId("platform_0");
		engine_1.setVersion("0.0.0");
		engine_1.setLocation(new Path("/some/location"));
		assertFalse(engine_1.isManaged());
	}

}
