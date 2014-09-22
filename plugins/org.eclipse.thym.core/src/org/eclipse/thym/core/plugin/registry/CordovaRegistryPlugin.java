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
package org.eclipse.thym.core.plugin.registry;

import java.util.ArrayList;
import java.util.List;

public class CordovaRegistryPlugin extends CordovaRegistryPluginInfo {
	
	private List<CordovaRegistryPluginVersion> versions;

	public List<CordovaRegistryPluginVersion> getVersions() {
		return versions;
	}

	public void addVersion(CordovaRegistryPluginVersion version ) {
		if(versions == null ){
			versions = new ArrayList<CordovaRegistryPluginVersion>();
		}
		versions.add(version);
	}
	
	/**
	 * Returns the {@link CordovaRegistryPluginVersion} object for the given 
	 * version. If the given version is not available it returns null
	 * @param version
	 * @return version or null
	 */
	public CordovaRegistryPluginVersion getVersion(String version){
		if(versions == null ){
			return null;
		}
		for (CordovaRegistryPluginVersion ver : versions) {
			if(ver.getVersionNumber().equals(version)){
				return ver;
			}
		}
		return null;
	}
}
