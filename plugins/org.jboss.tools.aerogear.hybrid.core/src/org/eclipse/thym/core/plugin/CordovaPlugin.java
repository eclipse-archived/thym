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
package org.eclipse.thym.core.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.PlatformLibrary;

import com.github.zafarkhaja.semver.Version;

public class CordovaPlugin extends PlatformObject{
	
	private class EngineDefinition {
		String name;
		String version;
		String platform;
	}

	private String id;
	private String version;
	private String name;
	private String description;
	private String license;
	private String author;
	private String keywords;
	private List<EngineDefinition> supportedEngines;
	private String info;
	private List<PluginJavaScriptModule> modules;
	private IFolder folder;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public void addSupportedEngine(String name, String version, String platform) {
		if (supportedEngines == null) {
			supportedEngines = new ArrayList<EngineDefinition>();
		}
		EngineDefinition engine = new EngineDefinition();
		engine.name = name;
		engine.version = version;
		engine.platform = platform;
		
		supportedEngines.add(engine);
	}
	

	public List<PluginJavaScriptModule> getModules() {
		if (modules == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(modules);
	}

	public void addModule(PluginJavaScriptModule module) {
		if (this.modules == null) {
			modules = new ArrayList<PluginJavaScriptModule>();
		}
		this.modules.add(module);
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
	
	public IFolder getFolder(){
		return folder;
	}

	public void setFolder(IFolder adapter) {
		this.folder = adapter;
	}
	
	/**
	 * Checks if the given engine is compatible with this plug-in. 
	 * Returns a {@link MultiStatus} as there may be more than one 
	 * reason for an engine to fail. 
	 * 
	 * @param engine
	 * @return A WARNING or OK level status
	 * 
	 */
	public IStatus isEngineCompatible(HybridMobileEngine engine) {
		if(supportedEngines == null || supportedEngines.isEmpty() )
			return Status.OK_STATUS;
		MultiStatus status = new MultiStatus(HybridCore.PLUGIN_ID, 0, NLS.bind("Plug-in {0} is not compatible with {1} version {2}" , new Object[] {getLabel(), engine.getName(), engine.getVersion()}),null);
		for (EngineDefinition definition : supportedEngines) {
			status.add(isDefinitionSatisfied(definition, engine));
		}
		return status;
	}
	
	private IStatus isDefinitionSatisfied(EngineDefinition definition, HybridMobileEngine engine){
		String reason;
		if(engine.getId().equals(definition.name)){// Engine ids match 
			Version engineVer = Version.valueOf(engine.getVersion());
			if(engineVer.satisfies(definition.version)){ // version is satisfied
				List<PlatformLibrary> enginePlatforms = engine.getPlatformLibs();
				for (PlatformLibrary ep : enginePlatforms) {	
					if(definition.platform == null ||
							"*".equals(definition.platform)||
							definition.platform.contains(ep.getPlatformId()))
					{
						return Status.OK_STATUS;
					}
				}
				reason = "engine platform: "+definition.platform;
			}else{
				reason = "engine version: "+definition.version;
			}
			
		}else{
			reason = "engine id: "+definition.name;
		}
		return new Status(IStatus.WARNING, HybridCore.PLUGIN_ID, 
				NLS.bind("Plug-in {0} does not support {1} version {2}. Fails version requirement: {3}",new Object[]{getLabel(),engine.getName(), engine.getVersion(), reason}));
	}
	
	/**
	 * Returns a label for the plug-in that can best be 
	 * presented. 
	 * 
	 * @return a label string
	 */
	public String getLabel(){
		return getName() != null ?getName() : getId();
	}
	
	@Override
	public String toString() {
		if(getId() == null )
			return super.toString();
		return getId()+(getVersion() ==null?"":"("+ getVersion()+")");
	}
	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof CordovaPlugin) {
			CordovaPlugin that = (CordovaPlugin) obj;
			return this.getId().equals(that.getId());
		}
		return super.equals(obj);
	}



}
