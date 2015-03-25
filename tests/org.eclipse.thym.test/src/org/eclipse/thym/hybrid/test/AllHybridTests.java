/*******************************************************************************
 * Copyright (c) 2013, 2015 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.hybrid.test;

import org.eclipse.thym.core.config.WidgetModelTest;
import org.eclipse.thym.core.plugin.test.CordovaPluginRegistryTest;
import org.eclipse.thym.core.plugin.test.InstallActionsTest;
import org.eclipse.thym.core.plugin.test.PluginInstallationTests;
import org.eclipse.thym.core.test.FileUtilsTest;
import org.eclipse.thym.core.test.HybridMobileEngineTests;
import org.eclipse.thym.core.test.HybridProjectConventionsTest;
import org.eclipse.thym.core.test.TestBundleHttpStorage;
import org.eclipse.thym.hybrid.test.ios.pbxproject.PBXProjectTest;
import org.eclipse.thym.ui.wizard.project.HybridProjectConvertTest;
import org.eclipse.thym.ui.wizard.project.HybridProjectCreatorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ FileUtilsTest.class, HybridProjectCreatorTest.class,HybridProjectConvertTest.class, 
	WidgetModelTest.class, CordovaPluginRegistryTest.class,HybridProjectConventionsTest.class, HybridMobileEngineTests.class,
	InstallActionsTest.class,PluginInstallationTests.class,PBXProjectTest.class,IntegrityTest.class, TestBundleHttpStorage.class,PluginXMLHelperTests.class})
public class AllHybridTests {

}
