/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.plugin.registry.plugin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CordovaRegistryPluginVersion {

	@SerializedName("name")
	@Expose
	private String name;

	@SerializedName("version")
	@Expose
	private String version;

	@SerializedName("dist")
	@Expose
	private CordovaPluginRegistryVersionDist dist;

	@SerializedName("license")
	@Expose
	private String license;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public CordovaPluginRegistryVersionDist getDist() {
		return dist;
	}

	public void setDist(CordovaPluginRegistryVersionDist dist) {
		this.dist = dist;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}
}
