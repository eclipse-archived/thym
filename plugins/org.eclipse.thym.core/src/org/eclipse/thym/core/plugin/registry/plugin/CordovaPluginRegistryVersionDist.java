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

public class CordovaPluginRegistryVersionDist {

	@SerializedName("shasum")
	@Expose
	private String shasum;

	@SerializedName("tarball")
	@Expose
	private String tarball;

	public String getShasum() {
		return shasum;
	}

	public void setShasum(String shasum) {
		this.shasum = shasum;
	}

	public String getTarball() {
		return tarball;
	}

	public void setTarball(String tarball) {
		this.tarball = tarball;
	}

}