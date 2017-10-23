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
import org.eclipse.thym.core.internal.util.EngineUtils;
import org.eclipse.thym.core.jobs.JobUtils;
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

	public static final String PROJECT_NAME1 = "HybridToolsTest1";
	public static final String APPLICATION_NAME1 = "Test applciation1";
	public static final String APPLICATION_ID1 = "hybrid.tools.test1";

	@Before
	public void setUpHybridMobileManager() throws CoreException {
		testproject = new TestProject();
		manager = new HybridMobileEngineManager(testproject.hybridProject());

		testProjectWithoutEngine = new TestProject(false, PROJECT_NAME1, APPLICATION_NAME1, APPLICATION_ID1);
		managerWithoutEngine = new HybridMobileEngineManager(testProjectWithoutEngine.hybridProject());
	}

	@After
	public void cleanUpHybridMobileManager() throws CoreException {
		if (testproject != null) {
			testproject.delete();
			testproject = null;
		}
		if (manager != null) {
			manager = null;
		}
		if (testProjectWithoutEngine != null) {
			testProjectWithoutEngine.delete();
			testProjectWithoutEngine = null;
		}
	}

	@Test
	public void testHybridMobileManagerActiveEngines() throws CoreException {
		// Test project is created with default engines so we expect them to be equal.
		List<HybridMobileEngine> defaultEngines = provider.defaultEngines();
		assertArrayEquals(defaultEngines.toArray(new HybridMobileEngine[defaultEngines.size()]), manager.getEngines());

		// Project has no engine
		assertTrue(managerWithoutEngine.getEngines().length == 0);
	}

	@Test
	public void testHybridMobileManagerUpdateEngines() throws CoreException {
		final HybridMobileEngine[] engines = new HybridMobileEngine[2];
		engines[0] = new HybridMobileEngine("android", "6.0.0", null);
		engines[1] = new HybridMobileEngine("ios", "4.4.0", null);
		Widget w = WidgetModel.getModel(testproject.hybridProject()).getWidgetForRead();
		assertEquals(provider.defaultEngines().size(), w.getEngines().size());
		assertEquals(provider.defaultEngines().size(), manager.getEngines().length);
		manager.updateEngines(engines);
		JobUtils.waitForIdle();
		// Run on a IWorkspaceRunnable because it needs to sync with the udpateEngines
		// call.
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				Widget w = WidgetModel.getModel(testproject.hybridProject()).getWidgetForRead();
				assertEquals(engines.length, w.getEngines().size());
				assertEquals(engines.length, manager.getEngines().length);
				checkEnginesPersistedCorrectly(engines);
			}
		};
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		ISchedulingRule rule = ws.getRuleFactory().modifyRule(testproject.getProject());
		ws.run(runnable, rule, 0, new NullProgressMonitor());
	}

	// Check given set of engines are persisted to config.xml correctly
	private void checkEnginesPersistedCorrectly(final HybridMobileEngine[] engines) throws CoreException {
		Widget w = WidgetModel.getModel(testproject.hybridProject()).getWidgetForRead();
		assertEquals(engines.length, w.getEngines().size());
		List<Engine> persistedEngines = w.getEngines();
		for (HybridMobileEngine hybridMobileEngine : engines) {
			boolean enginePersisted = false;
			for (Engine engine : persistedEngines) {
				if (hybridMobileEngine.getName().equals(engine.getName())
						&& EngineUtils.getExactVersion(hybridMobileEngine.getSpec())
								.equals(EngineUtils.getExactVersion(engine.getSpec()))) {
					enginePersisted = true;
					break;
				}
			}
			assertTrue("HybridMobile Engine is not persisted correctly", enginePersisted);
		}
	}

	@Test
	public void testAddEngine() throws CoreException {
		final HybridMobileEngine[] engines = new HybridMobileEngine[2];
		engines[0] = new HybridMobileEngine("android", "6.0.0", null);
		engines[1] = new HybridMobileEngine("ios", "4.4.0", null);
		final HybridMobileEngine newEngine = new HybridMobileEngine("windows", "5.0.0", null);
		manager.updateEngines(engines);
		JobUtils.waitForIdle();

		// Run on a IWorkspaceRunnable because it needs to sync with the udpateEngines
		// call.
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				Widget w = WidgetModel.getModel(testproject.hybridProject()).getWidgetForRead();
				assertEquals(engines.length, w.getEngines().size());
				assertEquals(engines.length, manager.getEngines().length);
				manager.addEngine(newEngine.getName(), newEngine.getSpec(), monitor, true);
				w = WidgetModel.getModel(testproject.hybridProject()).getWidgetForRead();
				assertEquals(engines.length + 1, w.getEngines().size());
				assertEquals(engines.length + 1, manager.getEngines().length);
				checkEnginesPersistedCorrectly(new HybridMobileEngine[] { engines[0], engines[1], newEngine });
			}
		};
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		ISchedulingRule rule = ws.getRuleFactory().modifyRule(testproject.getProject());
		ws.run(runnable, rule, 0, new NullProgressMonitor());
	}

	@Test
	public void testRemoveEngineUsingName() throws CoreException {
		final HybridMobileEngine[] engines = new HybridMobileEngine[2];
		engines[0] = new HybridMobileEngine("android", "6.0.0", null);
		engines[1] = new HybridMobileEngine("ios", "4.4.0", null);
		manager.updateEngines(engines);
		JobUtils.waitForIdle();

		// Run on a IWorkspaceRunnable because it needs to sync with the udpateEngines
		// call.
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				manager.removeEngine(engines[0].getName(), monitor, true);
				Widget w = WidgetModel.getModel(testproject.hybridProject()).getWidgetForRead();
				assertEquals(1, w.getEngines().size());
				assertEquals(1, manager.getEngines().length);
				checkEnginesPersistedCorrectly(new HybridMobileEngine[] { engines[1] });
			}
		};
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		ISchedulingRule rule = ws.getRuleFactory().modifyRule(testproject.getProject());
		ws.run(runnable, rule, 0, new NullProgressMonitor());
	}

	@Test
	public void testRemoveEngine() throws CoreException {
		final HybridMobileEngine[] engines = new HybridMobileEngine[2];
		engines[0] = new HybridMobileEngine("android", "6.0.0", null);
		engines[1] = new HybridMobileEngine("ios", "4.4.0", null);
		manager.updateEngines(engines);
		JobUtils.waitForIdle();

		// Run on a IWorkspaceRunnable because it needs to sync with the udpateEngines
		// call.
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				manager.removeEngine(engines[0], monitor, true);
				Widget w = WidgetModel.getModel(testproject.hybridProject()).getWidgetForRead();
				assertEquals(1, w.getEngines().size());
				checkEnginesPersistedCorrectly(new HybridMobileEngine[] { engines[1] });
			}
		};
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		ISchedulingRule rule = ws.getRuleFactory().modifyRule(testproject.getProject());
		ws.run(runnable, rule, 0, new NullProgressMonitor());
	}

	@Test
	public void testHybridMobileEngineEquals() {
		HybridMobileEngine engine_0 = new HybridMobileEngine("platform_0", "0.0.0", null);
		HybridMobileEngine engine_1 = new HybridMobileEngine("platform_0", "0.0.0", null);
		assertEquals(engine_0, engine_1);
	}

	@Test
	public void testManagerHasEngines() {
		assertFalse(managerWithoutEngine.hasActiveEngine());
		assertTrue(manager.hasActiveEngine());
	}
}
