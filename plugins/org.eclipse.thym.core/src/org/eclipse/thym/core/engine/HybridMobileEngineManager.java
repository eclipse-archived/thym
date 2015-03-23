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
package org.eclipse.thym.core.engine;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.config.Engine;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.core.engine.internal.cordova.CordovaEngineProvider;
/**
 * API for managing the engines for a {@link HybridProject}.
 * 
 * @author Gorkem Ercan
 *
 */
public class HybridMobileEngineManager {
	
	private final HybridProject project;
	
	public HybridMobileEngineManager(HybridProject project){
		this.project = project;
	}

	/**
	 * Returns the effective engines for project. 
	 * Active engines are determined as follows. 
	 * <ol>
	 * 	<li>
	 * if <i>engine</i> entries exist on config.xml match them to installed cordova engines. 
	 * 	</li>
	 * 	<li>
	 * if no <i>engine</i> entries exists on config.xml returns the default engines.
	 * if default engines can be determined.
	 * 	</li>
	 * @see HybridMobileEngineManager#defaultEngines()
	 * @return possibly empty array of {@link HybridMobileEngine}s
	 */
	public HybridMobileEngine[] getActiveEngines(){
		try{
			WidgetModel model = WidgetModel.getModel(project);
			Widget w = model.getWidgetForRead();
			List<Engine> engines = null; 
			if(w != null ){
				engines = w.getEngines();
			}
			if(engines == null || engines.isEmpty() ){
				HybridCore.log(IStatus.INFO, "No engine information exists on config.xml. Falling back to default engines",null );
				return defaultEngines();
			}
			CordovaEngineProvider engineProvider = new CordovaEngineProvider();
			ArrayList<HybridMobileEngine> activeEngines = new ArrayList<HybridMobileEngine>();
			final List<HybridMobileEngine> availableEngines = engineProvider.getAvailableEngines();
			for (Engine engine : engines) {
				for (HybridMobileEngine hybridMobileEngine : availableEngines) {
					if(engineMatches(engine, hybridMobileEngine)){
						activeEngines.add(hybridMobileEngine);
						break;
					}
				}
			}
			return activeEngines.toArray(new HybridMobileEngine[activeEngines.size()]);
		} catch (CoreException e) {
			HybridCore.log(IStatus.WARNING, "Engine information can not be read", e);
		}
		HybridCore.log(IStatus.WARNING, "Could not determine the engines used", null);
		return new HybridMobileEngine[0];
	}

	private boolean engineMatches(Engine configEngine, HybridMobileEngine engine){
		//null checks needed: sometimes we encounter engines without a name or version attribute. 
		return configEngine.getName() != null && configEngine.getName().equals(engine.getId()) &&
				configEngine.getVersion() != null && configEngine.getVersion().equals(engine.getVersion());
	}
	
	public static HybridMobileEngine[] defaultEngines() {
		CordovaEngineProvider engineProvider = new CordovaEngineProvider();
		List<HybridMobileEngine> availableEngines = engineProvider.getAvailableEngines();
		if(availableEngines == null || availableEngines.isEmpty() ){
			return new HybridMobileEngine[0];
		}
		ArrayList<HybridMobileEngine> defaults = new ArrayList<HybridMobileEngine>();
		for (HybridMobileEngine hybridMobileEngine : availableEngines) {
			boolean skip=false;
			//TODO: find the most recent version per platform too. 
			for (HybridMobileEngine defaultEngine : defaults) {
				if(hybridMobileEngine.getId().equals(defaultEngine.getId())){
					skip= true;
				}
			}
			if(!skip){
				defaults.add(hybridMobileEngine);
			}
		}
		return defaults.toArray(new HybridMobileEngine[defaults.size()]);
	}

	/**
	 * Persists the engine information. This either updates the existing 
	 * information per platform or creates a new one if it does not exist.
	 * 
	 * @param engine
	 * @throws CoreException
	 */
	public void updateEngine(HybridMobileEngine engine) throws CoreException{
		WidgetModel model = WidgetModel.getModel(project);
		Widget w = model.getWidgetForEdit();
		List<Engine> existingEngines = w.getEngines();
		Engine saveEngine = null;
		if(existingEngines != null ){
			for (Engine e: existingEngines) {//Check if an existing entry for the platform exists
				if(e.getName().equals(engine.getId())){
					saveEngine = e;
					w.removeEngine(e);//remove here to avoid duplicates
					break;
				}
			}
		}
		if(saveEngine == null ){
			saveEngine = model.createEngine(w);
		}
		saveEngine.setName(engine.getId());
		saveEngine.setVersion(engine.getVersion());
		w.addEngine(saveEngine);
		model.save();
	}

}
