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
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
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
import org.eclipse.thym.core.internal.cordova.CordovaCLIResult;
import org.eclipse.thym.core.internal.cordova.CordovaProjectCLI;
import org.eclipse.thym.core.internal.cordova.CordovaProjectCLI.Command;
import org.eclipse.thym.core.internal.util.EngineUtils;
import org.eclipse.thym.core.internal.cordova.ErrorDetectingCLIResult;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.core.plugin.PluginMessagesCLIResult;

import com.google.gson.JsonElement;
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
	public HybridMobileEngine[] getEngines(){
		HybridMobileEngine[] platformJsonEngines = getEnginesFromPlatformsJson();
		if (platformJsonEngines.length > 0) {
			return platformJsonEngines;
		}

		try{
			WidgetModel model = WidgetModel.getModel(project);
			Widget w = model.getWidgetForRead();
			if(w != null ){
				List<HybridMobileEngine> engines = new ArrayList<HybridMobileEngine>();
				List<Engine> configEngines = w.getEngines();
				if(configEngines != null){
					for(Engine e: configEngines){
						HybridMobileEngine engine = CordovaEngineProvider.getInstance()
								.createEngine(e.getName(), EngineUtils.getExactVersion(e.getSpec()));
						engines.add(engine);
					}
				}
				return engines.toArray(new HybridMobileEngine[engines.size()]);
			}
		} catch (CoreException e) {
			HybridCore.log(IStatus.WARNING, "Engine information can not be read", e);
		}
		return new HybridMobileEngine[0];
	}
	
	public void removeEngine(HybridMobileEngine engine, IProgressMonitor monitor, boolean save) throws OperationCanceledException, CoreException {
		if(monitor == null ) {
			monitor = new NullProgressMonitor();
		}
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		subMonitor.setTaskName("Removing platform: " + engine);	
		String options ="";
		if(save){
			options = CordovaProjectCLI.OPTION_SAVE;
		}
		IStatus status = project.getProjectCLI()
			.platform(Command.REMOVE, subMonitor.split(90), engine.getName(), options)
			.convertTo(PluginMessagesCLIResult.class)
			.asStatus();
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE, subMonitor.split(10, SubMonitor.SUPPRESS_ALL_LABELS));
		if(!status.isOK()){
			throw new CoreException(status);
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
	public HybridMobileEngine[] getEnginesFromPlatformsJson(){
		try {
			IFile file = project.getProject().getFile(PlatformConstants.PLATFORMS_JSON_PATH);
			if (!file.exists()) {
				return new HybridMobileEngine[0];
			}

			List<HybridMobileEngine> engines = new ArrayList<HybridMobileEngine>();

			JsonParser parser = new JsonParser();
			JsonObject root = parser.parse(new InputStreamReader(file.getContents())).getAsJsonObject();
			Set<Entry<String, JsonElement>> elements = root.entrySet();
			for(Entry<String, JsonElement> element: elements){
				String key = element.getKey();
				JsonElement valueElement = element.getValue();
				HybridMobileEngine engine = CordovaEngineProvider.getInstance().createEngine(key, valueElement.getAsString());
				engines.add(engine);
			}
			return engines.toArray(new HybridMobileEngine[engines.size()]);

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
				CordovaProjectCLI cordova = project.getProjectCLI();
				SubMonitor sm = SubMonitor.convert(monitor,100);
				if(existingEngines != null ){
					List<HybridMobileEngine> enginesList = Arrays.asList(engines);
					for (Engine existingEngine : existingEngines) {
						if(enginesList.contains(existingEngine)){
							cordova.platform(Command.REMOVE, sm,existingEngine.getName());
						}
						w.removeEngine(existingEngine);
					}
				}
				sm.worked(30);
				for (HybridMobileEngine engine : engines) {
					Engine e = model.createEngine(w);
					e.setName(engine.getName());
					e.setSpec(engine.getSpec());
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
				HybridMobileEngine[] activeEngines = getEnginesFromPlatformsJson();
				CordovaProjectCLI cordova = project.getProjectCLI();
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
							subStatus = cordova.platform(Command.REMOVE, loopMonitor.newChild(1), engine.getName())
									.convertTo(ErrorDetectingCLIResult.class).asStatus();
							status.add(subStatus);
						}
					} else {
						SubMonitor loopMonitor = sm.newChild(70).setWorkRemaining(configEngines.size());
						for (Engine e : configEngines) {
							String platformVersion = EngineUtils.getExactVersion(e.getSpec());
							String platformSpec = e.getName() + "@" + platformVersion;
							CliPlatform p = checkPlatformInstalled(e.getName());
							if (p == null) {
								subStatus = cordova.platform(Command.ADD, loopMonitor.newChild(1), platformSpec)
										.convertTo(ErrorDetectingCLIResult.class).asStatus();
							} else if (p != null && !p.version.equals(platformVersion)){
								//update only if version of installed platform changed
									subStatus = cordova.platform(Command.UPDATE, loopMonitor.newChild(1), platformSpec)
											.convertTo(ErrorDetectingCLIResult.class).asStatus();	
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
	 * Checks if project has at least one active engine
	 * @return true if project has active engine, false otherwise
	 */
	public boolean hasActiveEngine(){
		return getEngines().length > 0;
	}

	private CliPlatform checkPlatformInstalled(String engineName) throws CoreException {
		CordovaProjectCLI cli = project.getProjectCLI();
		CordovaCLIResult result = cli.platform(Command.LIST, new NullProgressMonitor(), "");
		String message = result.getMessage();
		int index = message.indexOf("Installed platforms:");
		int endIndex = message.indexOf("Available platforms:");
		if(index != -1 && endIndex != -1){
			String installedPlatformsMessage = message.substring(index, endIndex);
			String[] installedPlatforms = installedPlatformsMessage.split("\\r?\\n");
			for(String installedPlatform: installedPlatforms){
				String[] platformVersion = installedPlatform.split(" ");
				if(platformVersion.length > 3 && engineName.equals(platformVersion[2])){
					CliPlatform platform = new CliPlatform();
					platform.name = platformVersion[2];
					platform.version =platformVersion[3];
					return platform;
				}
			}
		} 
		return null;
	}
	
	private class CliPlatform {
		String name;
		String version;
	}

}
