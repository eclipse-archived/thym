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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CordovaRegistryPluginInfo {
	private String name;
	private String description;
	private List<String> keywords;
	private Map<String, String> maintainers;
	private String latestVersion;
	
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
	
	public void addKeyword(String keyword) {
		if(keywords == null ){
			keywords = new ArrayList<String>();
		}
		keywords.add(keyword);
	}
	
	public void addMaintainer(String email, String name){
		if(maintainers == null ){
			maintainers = new HashMap<String, String>();
		}
		maintainers.put(email, name);
	}
	
	public Map<String, String> getMaintainers(){
		return maintainers;
	}
	
	public String getLatestVersion() {
		return latestVersion;
	}

	public void setLatestVersion(String latestVersion) {
		this.latestVersion = latestVersion;
	}
	
	@Override
	public String toString() {
		if(getName() != null ){
			return getName();
		}
		return super.toString();
	}
	
}
