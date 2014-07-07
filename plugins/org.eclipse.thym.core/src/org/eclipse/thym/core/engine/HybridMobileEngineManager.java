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
package org.eclipse.thym.core.engine;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.engine.internal.cordova.CordovaEngineProvider;
import org.eclipse.thym.core.internal.util.ConfigJSon;
import org.eclipse.thym.core.platform.PlatformConstants;

public class HybridMobileEngineManager {
	
	private final HybridProject project;
	
	public HybridMobileEngineManager(HybridProject project){
		this.project = project;
	}

	public HybridMobileEngine getActiveEngine(){
		try{
			ConfigJSon configJson = ConfigJSon.readConfigJson(project.getProject());
			if(configJson == null || configJson.getEngineId() == null ){
				HybridCore.log(IStatus.WARNING, "No engine information on the config.json, this is OK if using an old project. Falling back to default engine.",null );
				return defaultEngine();
			}
			if(CordovaEngineProvider.CORDOVA_ENGINE_ID.equals(configJson.getEngineId()) || 
					CordovaEngineProvider.CUSTOM_CORDOVA_ENGINE_ID.equals(configJson.getEngineId())){
				CordovaEngineProvider engineProvider = new CordovaEngineProvider();
				HybridMobileEngine engine = engineProvider.getEngine(configJson.getEngineId(),configJson.getEngineVersion());
				return engine;
			}
			
		} catch (CoreException e) {
			HybridCore.log(IStatus.WARNING, "No existing engines can be created", e);
		}
		HybridCore.log(IStatus.WARNING, "Could not determine the engine used, falling back to default engine", null);
		return defaultEngine();
	}

	private HybridMobileEngine defaultEngine() {
		HybridMobileEngine engine = getDefaultEngine();
		if(engine == null ){
			CordovaEngineProvider engineProvider = new CordovaEngineProvider();
			engine =  engineProvider.createEngine(CordovaEngineProvider.CORDOVA_ENGINE_ID,"3.1.0");
		}
		return engine;
	}

	/**
	 * Returns the default engine defined by preferences or null if it is not 
	 * defined or does not exist anymore.
	 * 
	 * @return engine
	 */
	public static HybridMobileEngine getDefaultEngine() {
		CordovaEngineProvider engineProvider = new CordovaEngineProvider();
		String pref =  Platform.getPreferencesService().getString(PlatformConstants.HYBRID_UI_PLUGIN_ID, PlatformConstants.PREF_DEFAULT_ENGINE, null, null);
		if(pref != null && !pref.isEmpty()){
			String[] valuePair = pref.split(":");
			List<HybridMobileEngine> engines = engineProvider.getAvailableEngines();
			for (HybridMobileEngine engine : engines) {
				if(engine.getId().equals(valuePair[0]) && engine.getVersion().equals(valuePair[1])){
					return engine;
				}
			}
		}
		return null;
	}
	

	public void updateEngine(HybridMobileEngine engine) throws CoreException{
		ConfigJSon configJSon = ConfigJSon.readConfigJson(project.getProject());
		if(configJSon == null) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "config.json does not exist or can not be parsed. Updating the hybrid mobile engine for the project " + project.getProject().getName() +" failed."));
		}
		configJSon.setEngineInfo(engine);
		configJSon.persist(project.getProject());
	}

}
