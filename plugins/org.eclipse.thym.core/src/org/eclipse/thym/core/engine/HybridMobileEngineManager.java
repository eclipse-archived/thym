/*******************************************************************************
 * Copyright (c) 2013, 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.engine;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.config.Engine;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.core.engine.internal.cordova.CordovaEngineProvider;
import org.eclipse.thym.core.extensions.PlatformSupport;
import org.eclipse.thym.core.internal.cordova.CordovaProjectCLI;
import org.eclipse.thym.core.internal.cordova.CordovaProjectCLI.Command;
import org.eclipse.thym.core.internal.cordova.ErrorDetectingCLIResult;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.osgi.framework.Version;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
/**
 * API for managing the engines for a {@link HybridProject}.
 * 
 * @author Gorkem Ercan
 *
 */
public class HybridMobileEngineManager {
	
	private final HybridProject project;
	
	public HybridMobileEngineManager(HybridProject project){
		if(project == null ){
			throw new IllegalArgumentException("No project specified");
		}
		this.project = project;
	}

	/**
	 * Returns the effective engines for project. 
	 * Active engines are determined as follows. 
	 * <ol>
	 * 	<li>
	 * if any platforms are listed in the platforms.json file, these are returned first, without
	 * checking config.xml.
	 * 	</li>
	 * 	<li>
	 * if <i>engine</i> entries exist on config.xml match them to installed cordova engines. 
	 * 	</li>
	 * @see HybridMobileEngineManager#defaultEngines()
	 * @return possibly empty array of {@link HybridMobileEngine}s
	 */
	public HybridMobileEngine[] getActiveEngines(){
		HybridMobileEngine[] platformJsonEngines = getActiveEnginesFromPlatformsJson();
		if (platformJsonEngines.length > 0) {
			return platformJsonEngines;
		}

		try{
			WidgetModel model = WidgetModel.getModel(project);
			Widget w = model.getWidgetForRead();
			List<Engine> engines = null; 
			if(w != null ){
				engines = w.getEngines();
			}
			if(engines != null && !engines.isEmpty() ){
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
			}
		} catch (CoreException e) {
			HybridCore.log(IStatus.WARNING, "Engine information can not be read", e);
		}
		return new HybridMobileEngine[0];
	}

	private boolean engineMatches(Engine configEngine, HybridMobileEngine engine){
		//null checks needed: sometimes we encounter engines without a name or version attribute.
		if(engine.isManaged()){

			// Since cordova uses semver, version numbers in config.xml can begin with '~' or '^'.
			if (configEngine.getSpec() != null) {
				String spec = configEngine.getSpec();
				if (spec.startsWith("~") || spec.startsWith("^")) {
					spec = spec.substring(1);
				}
				return configEngine.getName() != null && configEngine.getName().equals(engine.getId())
						&& spec.equals(engine.getVersion());
			} else {
				return false;
			}
		}else{
			return engine.getLocation().isValidPath(configEngine.getSpec()) 
					&& engine.getLocation().equals(new Path(configEngine.getSpec()));
		}
	}

	/**
	 * Returns the active engines for the project by looking at
	 * the values stored in platforms.json.
	 *
	 * </p>
	 * If no engines are found in platforms.json, returns an empty array.
	 * The file platforms.json is where the currently active cordova engines
	 * are stored, (semi-)independently of what is stored in config.xml.
	 *
	 * @see HybridMobileEngineManager#defaultEngines()
	 * @see HybridMobileEngineManager#getActiveEngines()
	 * @return possibly empty array of {@link HybridMobileEngine}s
	 */
	public HybridMobileEngine[] getActiveEnginesFromPlatformsJson(){
		try {
			IFile file = project.getProject().getFile(PlatformConstants.PLATFORMS_JSON_PATH);
			if (!file.exists()) {
				return new HybridMobileEngine[0];
			}

			List<HybridMobileEngine> activeEngines = new ArrayList<HybridMobileEngine>();

			JsonParser parser = new JsonParser();
			JsonObject root = parser.parse(new InputStreamReader(file.getContents())).getAsJsonObject();
			for (PlatformSupport support : HybridCore.getPlatformSupports()) {
				String platform = support.getPlatformId();
				if (root.has(platform)) {
					HybridMobileEngine engine =
							getHybridMobileEngine(platform, root.get(platform).getAsString());
					if (engine != null) {
						activeEngines.add(engine);
					}
				}
			}
			return activeEngines.toArray(new HybridMobileEngine[activeEngines.size()]);

		} catch (JsonIOException e) {
			HybridCore.log(IStatus.WARNING, "Error reading input stream from platforms.json", e);
		} catch (JsonSyntaxException e) {
			HybridCore.log(IStatus.WARNING, "platforms.json has errors", e);
		} catch (CoreException e) {
			HybridCore.log(IStatus.WARNING, "Error while opening platforms.json", e);
		}
		return new HybridMobileEngine[0];
	}

	/**
	 * Returns the HybridMobileEngine that corresponds to the provide name and spec.
	 * Searches through available engines for a match, and may return null if no
	 * matching engine is found.
	 *
	 * @return The HybridMobileEngine corresponding to name and spec, or null if
	 * a match cannot be found.
	 */
	private HybridMobileEngine getHybridMobileEngine(String name, String spec) {
		CordovaEngineProvider engineProvider = new CordovaEngineProvider();
		final List<HybridMobileEngine> availableEngines = engineProvider.getAvailableEngines();
		for (HybridMobileEngine engine : availableEngines) {
			if (engine.isManaged()) {
				if (engine.getId().equals(name) && engine.getVersion().equals(spec)) {
					return engine;
				}
			} else {
				if (engine.getId().equals(name) && engine.getLocation().toString().equals(spec)) {
					return engine;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the {@link HybridMobileEngine}s specified within Thym preferences.
	 *
	 * </p>
	 * If no engines have been added, returns an empty array. Otherwise returns
	 * either the user's preference, or, by default, the most recent version
	 * available for each platform.
	 *
	 * @see HybridMobileEngineManager#getActiveEngines()
	 * @return possibly empty array of {@link HybridMobileEngine}s
	 */
	public static HybridMobileEngine[] defaultEngines() {
		CordovaEngineProvider engineProvider = new CordovaEngineProvider();
		List<HybridMobileEngine> availableEngines = engineProvider.getAvailableEngines();
		if(availableEngines == null || availableEngines.isEmpty() ){
			return new HybridMobileEngine[0];
		}
		ArrayList<HybridMobileEngine> defaults = new ArrayList<HybridMobileEngine>();
		
		String pref =  Platform.getPreferencesService().getString(PlatformConstants.HYBRID_UI_PLUGIN_ID, PlatformConstants.PREF_DEFAULT_ENGINE, null, null);
		if(pref != null && !pref.isEmpty()){
			String[] engineStrings = pref.split(",");
			for (String engineString : engineStrings) {
				String[] engineInfo = engineString.split(":");
				for (HybridMobileEngine hybridMobileEngine : availableEngines) {
					if (hybridMobileEngine.isManaged()) {
						if (engineInfo[0].equals(hybridMobileEngine.getId())
								&& engineInfo[1].equals(hybridMobileEngine.getVersion())) {
							defaults.add(hybridMobileEngine);
						}
					} else {
						if (engineInfo[0].equals(hybridMobileEngine.getId())
								&& engineInfo[1].equals(hybridMobileEngine.getLocation().toString())) {
							defaults.add(hybridMobileEngine);
						}
					}
				}
			}
		}else{
			HashMap<String, HybridMobileEngine> platforms = new HashMap<String, HybridMobileEngine>();
			for (HybridMobileEngine hybridMobileEngine : availableEngines) {
				if(platforms.containsKey(hybridMobileEngine.getId())){
					HybridMobileEngine existing = platforms.get(hybridMobileEngine.getId());
					try{
						Version ev = Version.parseVersion(existing.getVersion());
						Version hv = Version.parseVersion(hybridMobileEngine.getVersion());
						if(hv.compareTo(ev) >0 ){
							platforms.put(hybridMobileEngine.getId(), hybridMobileEngine);
						}
					}catch(IllegalArgumentException e){
						//catch the version parse errors because version field may actually contain 
						//git urls and local paths.
					}
				}else{
					platforms.put(hybridMobileEngine.getId(),hybridMobileEngine);
				}
			}
			defaults.addAll(platforms.values());
		}
		return defaults.toArray(new HybridMobileEngine[defaults.size()]);
	}

	/**
	 * Persists engine information to config.xml. 
	 * Removes existing engines form the project.
	 * Calls cordova prepare so that the new engines are restored.
	 * 
	 * @param engine
	 * @throws CoreException
	 */
	public void updateEngines(final HybridMobileEngine[] engines) throws CoreException{
		Assert.isLegal(engines != null, "Engines can not be null" );
		WorkspaceJob updateJob = new WorkspaceJob(NLS.bind("Update Cordova Engines for {0}",project.getProject().getName()) ) {
			
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {

				WidgetModel model = WidgetModel.getModel(project);
				Widget w = model.getWidgetForEdit();
				List<Engine> existingEngines = w.getEngines();
				CordovaProjectCLI cordova = CordovaProjectCLI.newCLIforProject(project);
				SubMonitor sm = SubMonitor.convert(monitor,100);
				if(existingEngines != null ){
					for (Engine existingEngine : existingEngines) {
						if(isEngineRemoved(existingEngine, engines)){
							cordova.platform(Command.REMOVE, sm,existingEngine.getName());
						}
						w.removeEngine(existingEngine);
					}
				}
				sm.worked(30);
				for (HybridMobileEngine engine : engines) {
					Engine e = model.createEngine(w);
					e.setName(engine.getId());
					if(!engine.isManaged()){
						e.setSpec(engine.getLocation().toString());
					}else{
						e.setSpec(engine.getVersion());
					}
					w.addEngine(e);
				}
				model.save();
				if(w.getEngines() != null && !w.getEngines().isEmpty()){
					project.prepare(sm.newChild(70), "");
				}
				sm.done();
				return Status.OK_STATUS;
			}
		};
		ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(this.project.getProject());
		updateJob.setRule(rule);
		updateJob.schedule();
	}

	/**
	 * Updates active Cordova engines based on what is written to
	 * config.xml by calling cordova update or cordova add, depending
	 * on context.
	 */
	public void resyncWithConfigXml() {
		WorkspaceJob prepareJob = new WorkspaceJob(NLS.bind("Updating project from config.xml for {0}",
				project.getProject().getName())) {

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				if (project == null) {
					String err = "Updating from config.xml: Could not get HybridProject";
					HybridCore.log(IStatus.WARNING, err, null);
					return new Status(IStatus.WARNING, HybridCore.PLUGIN_ID, err);
				}
				HybridMobileEngine[] activeEngines = getActiveEnginesFromPlatformsJson();
				CordovaProjectCLI cordova = CordovaProjectCLI.newCLIforProject(project);
				MultiStatus status = new MultiStatus(HybridCore.PLUGIN_ID, 0, 
						"Errors updating engines from config.xml", null);
				IStatus subStatus = Status.OK_STATUS;
				SubMonitor sm = SubMonitor.convert(monitor, 100);

				Widget widget = WidgetModel.getModel(project).getWidgetForEdit();
				if (widget != null) {
					List<Engine> configEngines = widget.getEngines();
					if (configEngines == null) {
						if(activeEngines == null){
							return status; 
						}
						SubMonitor loopMonitor = sm.newChild(70).setWorkRemaining(activeEngines.length);
						for(HybridMobileEngine engine: activeEngines){
							subStatus = cordova.platform(Command.REMOVE, loopMonitor.newChild(1), engine.getId())
									.convertTo(ErrorDetectingCLIResult.class).asStatus();
							status.add(subStatus);
						}
					} else {
						SubMonitor loopMonitor = sm.newChild(70).setWorkRemaining(configEngines.size());
						for (Engine e : configEngines) {
							String platformSpec = e.getName() + "@" + e.getSpec();
							if (!checkPlatformInstalled(activeEngines, e.getName())) {
								subStatus = cordova.platform(Command.ADD, loopMonitor.newChild(1), platformSpec)
										.convertTo(ErrorDetectingCLIResult.class).asStatus();
							} else {
								String engineVersion = e.getSpec().replaceAll("~", "");
								//update only if version of installed platform changed
								if(!engineVersion.equals(getInstalledPlatformVersion(activeEngines, e.getName()))){
									subStatus = cordova.platform(Command.UPDATE, loopMonitor.newChild(1), platformSpec)
											.convertTo(ErrorDetectingCLIResult.class).asStatus();
								}	
							}
							status.add(subStatus);
						}
					}
				}
				project.getProject().refreshLocal(IResource.DEPTH_INFINITE, sm.newChild(30));

				return status;
			}
		};

		ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRuleFactory()
				.modifyRule(project.getProject());
		prepareJob.setRule(rule);
		prepareJob.schedule();
	}
	
	/**
	 * Returns the active engine for the platform id or null if there is not one.
	 * 
	 * @param platformId
	 * @return active engine or null
	 */
	public HybridMobileEngine getActiveEngineForPlatform(String platformId){
		HybridMobileEngine[] engines = getActiveEngines();
		for (HybridMobileEngine hybridMobileEngine : engines) {
			if(platformId.equals(hybridMobileEngine.getId())){
				return hybridMobileEngine;
			}
		}
		return null;
	}
	
	/**
	 * Checks if project has at least one active engine
	 * @return true if project has active engine, false otherwise
	 */
	public boolean hasActiveEngine(){
		return getActiveEngines().length > 0;
	}

	private boolean checkPlatformInstalled(HybridMobileEngine[] activeEngines, String engineName) {
		for (HybridMobileEngine engine : activeEngines) {
			if (engine.getId().equals(engineName)) {
				return true;
			}
		}
		return false;
	}
	
	private String getInstalledPlatformVersion(HybridMobileEngine[] activeEngines, String engineName) {
		for (HybridMobileEngine engine : activeEngines) {
			if (engine.getId().equals(engineName)) {
				return engine.getVersion();
			}
		}
		return null;
	}

	private boolean isEngineRemoved(final Engine engine, final HybridMobileEngine[] engines){
		for (HybridMobileEngine hybridMobileEngine : engines) {
			if(hybridMobileEngine.getId().equals(engine.getName()) && hybridMobileEngine.getVersion().equals(engine.getSpec())){
				return false;
			}
		}
		return true;
	}

}
