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
package org.eclipse.thym.core.plugin.test;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPlugin;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPlugin.RegistryPluginVersion;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPluginInfo;
import org.eclipse.thym.core.plugin.registry.CordovaPluginRegistryManager;

import static org.junit.Assert.*;

import org.junit.Test;

public class CordovaPluginRegistryTest {
	
	@Test
	public void testRetrievePluginInfosFromCordovaRegistry() throws CoreException{
		CordovaPluginRegistryManager client = getCordovaIORegistryClient();
		List<CordovaRegistryPluginInfo> infos = client.retrievePluginInfos(new NullProgressMonitor());
		assertNotNull(infos);
		assertFalse(infos.isEmpty());
		CordovaRegistryPluginInfo info = infos.get(0);
		assertNotNull(info.getName());
	}

	@Test
	public void testReadCordovaPluginFromCordovaRegistry() throws CoreException{
		CordovaPluginRegistryManager client = getCordovaIORegistryClient();
		List<CordovaRegistryPluginInfo> infos = client.retrievePluginInfos(new NullProgressMonitor());
		CordovaRegistryPluginInfo info = infos.get(0);
		CordovaRegistryPlugin plugin = client.getCordovaPluginInfo(info.getName());
		assertNotNull(plugin);
		assertNotNull(plugin.getName());
		assertEquals(info.getName(), plugin.getName());
		assertNotNull(plugin.getVersions());
		List<RegistryPluginVersion> versions = plugin.getVersions();
		assertFalse(versions.isEmpty());
		RegistryPluginVersion version = versions.get(0);
		assertNotNull(version.getName());
		assertNotNull(version.getVersionNumber());
		assertNotNull(version.getShasum());
		assertNotNull(version.getTarball());
	}

	private CordovaPluginRegistryManager getCordovaIORegistryClient() {
		CordovaPluginRegistryManager client = new CordovaPluginRegistryManager();
		return client;
	}
}
