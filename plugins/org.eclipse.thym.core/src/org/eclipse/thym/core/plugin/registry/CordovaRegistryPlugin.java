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
package org.eclipse.thym.core.plugin.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CordovaRegistryPlugin extends CordovaRegistryPluginInfo {
	
	private List<RegistryPluginVersion> versions;
	private List<String> keywords;
	private Map<String, String> maintainers;
	private String latestVersion;
	private String license;
	
	public class RegistryPluginVersion{
		private String versionNumber;
		private String tarball;
		private String shasum;

		public String getVersionNumber() {
			return versionNumber;
		}

		public void setVersionNumber(String versionNumber) {
			this.versionNumber = versionNumber;
		}
		
		public String getName(){
			return getName();
		}

		public String getTarball() {
			return tarball;
		}

		public void setTarball(String tarball) {
			this.tarball = tarball;
		}

		public String getShasum() {
			return shasum;
		}

		public void setShasum(String shasum) {
			this.shasum = shasum;
		}
	}
	
	public List<RegistryPluginVersion> getVersions() {
		return versions;
	}

	public void addVersion(RegistryPluginVersion version ) {
		if(versions == null ){
			versions = new ArrayList<RegistryPluginVersion>();
		}
		versions.add(version);
	}
	
	/**
	 * Returns the {@link RegistryPluginVersion} object for the given 
	 * version. If the given version does not exist it returns null
	 * @param version
	 * @return version or null
	 */
	public RegistryPluginVersion getVersion(String version){
		if(versions == null ){
			return null;
		}
		for (RegistryPluginVersion ver : versions) {
			if(ver.getVersionNumber().equals(version)){
				return ver;
			}
		}
		return null;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void addKeyword(String keyword) {
		if(keywords == null ){
			keywords = new ArrayList<String>();
		}
		keywords.add(keyword);
	}

	public void addMaintainer(String email, String name) {
		if(maintainers == null ){
			maintainers = new HashMap<String, String>();
		}
		maintainers.put(email, name);
	}

	public Map<String, String> getMaintainers() {
		return maintainers;
	}

	public String getLatestVersion() {
		return latestVersion;
	}

	public void setLatestVersion(String latestVersion) {
		this.latestVersion = latestVersion;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}
}
