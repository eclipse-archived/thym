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

public class CordovaRegistryPluginVersion extends CordovaRegistryPluginInfo {
	private String versionNumber;
	private String distributionTarball;
	private String distributionSHASum;
	private String license;
	

	public String getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(String versionNumber) {
		this.versionNumber = versionNumber;
	}

	public String getDistributionTarball() {
		return distributionTarball;
	}

	public void setDistributionTarball(String distributionTarball) {
		this.distributionTarball = distributionTarball;
	}

	public String getDistributionSHASum() {
		return distributionSHASum;
	}

	public void setDistributionSHASum(String distributionSHASum) {
		this.distributionSHASum = distributionSHASum;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}
	
}
