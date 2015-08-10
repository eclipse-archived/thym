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
import org.eclipse.thym.core.plugin.registry.CordovaPluginRegistryMapper;

import static org.junit.Assert.*;

import org.junit.Test;

public class CordovaPluginRegistryTest {
	
	private static final String MAPPER_OLD_ID = "org.apache.cordova.console";
	private static final String MAPPER_NEW_ID = "cordova-plugin-console";

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
		List<RegistryPluginVersion> versions = plugin.getVersions();
		assertNotNull(versions);
		assertFalse(versions.isEmpty());
		RegistryPluginVersion version = versions.get(0);
		assertNotNull(version.getName());
		assertNotNull(version.getVersionNumber());
		assertNotNull(version.getShasum());
		assertNotNull(version.getTarball());
	}
	
	@Test
	public void testCordovaRegistryMapper_toOld(){
		String oldID = CordovaPluginRegistryMapper.toOld(MAPPER_NEW_ID); 
		assertEquals(MAPPER_OLD_ID, oldID);
		assertNull("unkown new id should return null",CordovaPluginRegistryMapper.toOld("some-unknown-id"));
	}

	@Test
	public void testCordovaRegistryMapper_toNew(){
		String newId = CordovaPluginRegistryMapper.toNew(MAPPER_OLD_ID);
		assertEquals(MAPPER_NEW_ID, newId);
		assertNull("unknow old  id should return null", CordovaPluginRegistryMapper.toNew("some.old.id"));
	}
	
	@Test
	public void testCordovaRegistryMapper_alternateId(){
		String alternate = CordovaPluginRegistryMapper.alternateID(MAPPER_OLD_ID);
		assertEquals(MAPPER_NEW_ID, alternate);
		alternate = CordovaPluginRegistryMapper.alternateID(MAPPER_NEW_ID);
		assertEquals(MAPPER_OLD_ID,alternate);
	}
	
	@Test
	public void testCordovaRegistryMapper_nullParams(){
		assertNull(CordovaPluginRegistryMapper.toNew(null));
		assertNull(CordovaPluginRegistryMapper.toOld(null));
	}
	
	private CordovaPluginRegistryManager getCordovaIORegistryClient() {
		CordovaPluginRegistryManager client = new CordovaPluginRegistryManager();
		return client;
	}
}
