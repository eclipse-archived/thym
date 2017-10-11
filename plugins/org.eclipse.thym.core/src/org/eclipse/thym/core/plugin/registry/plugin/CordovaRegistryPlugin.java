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

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CordovaRegistryPlugin {

	@SerializedName("name")
	@Expose
	private String name;

	@SerializedName("description")
	@Expose
	private String description;

	@SerializedName("keywords")
	@Expose
	private List<String> keywords;

	@SerializedName("versions")
	@Expose
	private List<CordovaRegistryPluginVersion> versions;

	@SerializedName("dist-tags")
	@Expose
	private DistTag distTags;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public List<CordovaRegistryPluginVersion> getVersions() {
		return versions;
	}

	public void setVersions(List<CordovaRegistryPluginVersion> versions) {
		this.versions = versions;
	}

	public void setDistTag(DistTag distTags) {
		this.distTags = distTags;
	}

	public CordovaRegistryPluginVersion getLatestVersion() {
		for (CordovaRegistryPluginVersion version : versions) {
			if (version.getVersion().equals(distTags.getLatest())) {
				return version;
			}
		}
		return null;

	}

	class DistTag {

		@SerializedName("latest")
		@Expose
		public String latest;

		public String getLatest() {
			return latest;
		}

		public void setLatest(String latest) {
			this.latest = latest;
		}
	}

}
