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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.thym.core.internal.cordova.CordovaProjectCLI;
import org.eclipse.thym.core.internal.cordova.CordovaProjectCLI.Command;
import org.eclipse.thym.core.internal.util.EngineUtils;
import org.eclipse.thym.core.plugin.PluginMessagesCLIResult;

/**
 * API for managing the engines for a {@link HybridProject}.
 * 
 * @author Gorkem Ercan
 *
 */
public class HybridMobileEngineManager {

	private final HybridProject project;

	public HybridMobileEngineManager(HybridProject project) {
		if (project == null) {
			throw new IllegalArgumentException("No project specified");
		}
		this.project = project;
	}

	/**
	 * Returns engines from config.xml
	 */
	public HybridMobileEngine[] getEngines() {
		try {
			WidgetModel model = WidgetModel.getModel(project);
			Widget w = model.getWidgetForRead();
			if (w != null) {
				List<HybridMobileEngine> engines = new ArrayList<HybridMobileEngine>();
				List<Engine> configEngines = w.getEngines();
				if (configEngines != null) {
					for (Engine e : configEngines) {
						HybridMobileEngine engine = CordovaEngineProvider.getInstance().createEngine(e.getName(),
								EngineUtils.getExactVersion(e.getSpec()));
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

	public void removeEngine(HybridMobileEngine engine, IProgressMonitor monitor, boolean save)
			throws OperationCanceledException, CoreException {
		removeEngine(engine.getName(), monitor, save);
	}
	
	public void removeEngine(String engineName, IProgressMonitor monitor, boolean save) 
			throws OperationCanceledException, CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		subMonitor.setTaskName("Removing engine: " + engineName);
		String options = "";
		if (save) {
			options = CordovaProjectCLI.OPTION_SAVE;
		} else {
			options = CordovaProjectCLI.OPTION_NO_SAVE;
		}
		IStatus status = project.getProjectCLI()
				.platform(Command.REMOVE, subMonitor.split(90), engineName, options)
				.convertTo(PluginMessagesCLIResult.class).asStatus();
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE,
				subMonitor.split(10, SubMonitor.SUPPRESS_ALL_LABELS));
		if (!status.isOK()) {
			throw new CoreException(status);
		}
	}
	
	public void addEngine(String engineName, String engineSpec, IProgressMonitor monitor, boolean save) 
			throws OperationCanceledException, CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		String fullSpec = engineName +"@"+engineSpec;
		subMonitor.setTaskName("Adding engine: " + fullSpec);
		String options = "";
		if (save) {
			options = CordovaProjectCLI.OPTION_SAVE;
		} else {
			options = CordovaProjectCLI.OPTION_NO_SAVE;
		}
		
		IStatus status = project.getProjectCLI()
				.platform(Command.ADD, subMonitor.split(90), fullSpec, options)
				.convertTo(PluginMessagesCLIResult.class).asStatus();
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE,
				subMonitor.split(10, SubMonitor.SUPPRESS_ALL_LABELS));
		if (!status.isOK()) {
			throw new CoreException(status);
		}
	}

	/**
	 * Persists engine information to config.xml. Removes existing engines from the
	 * project. Calls cordova prepare so that the new engines are restored.
	 * 
	 * @param engine
	 * @throws CoreException
	 */
	public void updateEngines(final HybridMobileEngine[] engines) throws CoreException {
		Assert.isLegal(engines != null, "Engines can not be null");
		WorkspaceJob updateJob = new WorkspaceJob(
				NLS.bind("Update Cordova Engines for {0}", project.getProject().getName())) {

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {

				WidgetModel model = WidgetModel.getModel(project);
				Widget w = model.getWidgetForEdit();
				List<Engine> existingEngines = w.getEngines();
				SubMonitor sm = SubMonitor.convert(monitor, 100);
				if (existingEngines != null) {
					for (Engine existingEngine : existingEngines) {
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
				if (w.getEngines() != null && !w.getEngines().isEmpty()) {
					project.prepare(sm.newChild(70));
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
	 * Checks if project has at least one active engine
	 * 
	 * @return true if project has active engine, false otherwise
	 */
	public boolean hasActiveEngine() {
		return getEngines().length > 0;
	}

}
