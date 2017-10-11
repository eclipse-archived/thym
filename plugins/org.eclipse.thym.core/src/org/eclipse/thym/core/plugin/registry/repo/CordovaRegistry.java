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
package org.eclipse.thym.core.plugin.registry.repo;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CordovaRegistry {

	@SerializedName("results")
	@Expose
	private List<CordovaRegistrySearchPlugin> cordovaPlugins = null;
	@SerializedName("total")
	@Expose
	private Integer total;

	public List<CordovaRegistrySearchPlugin> getCordovaPlugins() {
		List<CordovaRegistrySearchPlugin> filteredPlugins = new ArrayList<>();
		for (CordovaRegistrySearchPlugin plugin : cordovaPlugins) {
			if (!plugin.getKeywords().contains("cordova:platform")) {
				filteredPlugins.add(plugin);
			}
		}
		return filteredPlugins;
	}

	public void setCordovaPlugins(List<CordovaRegistrySearchPlugin> cordovaPlugins) {
		this.cordovaPlugins = cordovaPlugins;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

}