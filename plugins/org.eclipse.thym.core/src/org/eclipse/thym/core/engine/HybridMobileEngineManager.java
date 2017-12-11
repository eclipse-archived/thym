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
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
import org.eclipse.thym.core.platform.PlatformConstants;
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
		if (!isPlatformInstalled(engineName)) {
			return;
		}
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
		IStatus status = project.getProjectCLI().platform(Command.REMOVE, subMonitor.split(90), engineName, options)
				.convertTo(PluginMessagesCLIResult.class).asStatus();
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE,
				subMonitor.split(10, SubMonitor.SUPPRESS_ALL_LABELS));
		if (!status.isOK()) {
			throw new CoreException(status);
		}
	}

	public void addEngine(String engineName, String engineSpec, IProgressMonitor monitor, boolean save)
			throws OperationCanceledException, CoreException {
		if (isPlatformInstalled(engineName)) {
			return;
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		String fullSpec = engineName + "@" + engineSpec;
		subMonitor.setTaskName("Adding engine: " + fullSpec);
		String options = "";
		if (save) {
			options = CordovaProjectCLI.OPTION_SAVE;
		} else {
			options = CordovaProjectCLI.OPTION_NO_SAVE;
		}

		IStatus status = project.getProjectCLI().platform(Command.ADD, subMonitor.split(90), fullSpec, options)
				.convertTo(PluginMessagesCLIResult.class).asStatus();
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE,
				subMonitor.split(10, SubMonitor.SUPPRESS_ALL_LABELS));
		if (!status.isOK()) {
			throw new CoreException(status);
		}
	}

	private boolean isPlatformInstalled(String platformName) {
		if (platformName == null) {
			return false;
		}
		try {
			IFolder platformHome = getPlatformHomeFolder(platformName);
			if (platformHome != null) {
				IFile platformJson = platformHome.getFile(platformName + ".json");
				return platformJson.getLocation() != null && platformJson.getLocation().toFile().exists();
			}
		} catch (CoreException e) {
			// ignore to return false
		}
		return false;
	}

	private IFolder getPlatformHomeFolder(String platform) throws CoreException {
		if (platform == null) {
			return null;
		}
		IFolder platforms = getPlatformsFolder();
		if (!platforms.exists()) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Platforms folder does not exist"));
		}
		IFolder platformHome = platforms.getFolder(platform);
		IPath location = platformHome.getLocation();
		if (platformHome.exists() && location != null && location.toFile().isDirectory()) {
			return platformHome;
		}
		return null;
	}

	private IFolder getPlatformsFolder() {
		IFolder platforms = this.project.getProject().getFolder(PlatformConstants.DIR_PLATFORMS);
		return platforms;
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

				HybridMobileEngine[] existingEngines = getEngines();

				List<HybridMobileEngine> newEngines = Arrays.asList(engines);
				List<HybridMobileEngine> oldEngines = Arrays.asList(existingEngines);

				List<HybridMobileEngine> toInstall = new ArrayList<>();

				List<HybridMobileEngine> toUninstall = new ArrayList<>();

				List<HybridMobileEngine> enginesToUpdate = new ArrayList<>();

				for (HybridMobileEngine oldEngine : oldEngines) {
					boolean engineFound = false;
					for (HybridMobileEngine newEngine : newEngines) {
						if (newEngine.getName().equals(oldEngine.getName())) {
							if (!newEngine.getSpec().equals(oldEngine.getSpec())) {
								enginesToUpdate.add(newEngine);
							}
							engineFound = true;
							break;
						}
					}
					if (!engineFound) {
						toUninstall.add(oldEngine);
					}
				}

				for (HybridMobileEngine newEngine : newEngines) {
					boolean engineFound = false;
					for (HybridMobileEngine oldEngine : oldEngines) {
						if (oldEngine.getName().equals(newEngine.getName())) {
							engineFound = true;
							break;
						}
					}
					if (!engineFound) {
						toInstall.add(newEngine);
					}
				}

				SubMonitor subMonitor = SubMonitor.convert(monitor);
				subMonitor.setWorkRemaining(toInstall.size() + toUninstall.size() + enginesToUpdate.size() * 2);
				for (HybridMobileEngine uninstall : toUninstall) {
					try {
						removeEngine(uninstall, subMonitor.split(1), true);
					} catch (CoreException e) {
						HybridCore.log(Status.ERROR, "Unable to remove engine " + uninstall.getName(), e);
					}
				}
				for (HybridMobileEngine install : toInstall) {
					try {
						addEngine(install.getName(), install.getSpec(), subMonitor.split(1), true);
					} catch (CoreException e) {
						HybridCore.log(Status.ERROR, "Unable to add engine " + install.getName(), e);
					}
				}

				// update versions
				for (HybridMobileEngine engineToUpdate : enginesToUpdate) {
					try {
						// some engines do not support "in place" update. They require platform remove,
						// then platform add
						project.getEngineManager().removeEngine(engineToUpdate, subMonitor.split(1), true);
						project.getEngineManager().addEngine(engineToUpdate.getName(), engineToUpdate.getSpec(),
								subMonitor.split(1), true);
					} catch (CoreException e) {
						HybridCore.log(Status.ERROR, "Unable to update engine " + engineToUpdate.getName(), e);
					}
				}
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
