/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Red Hat Inc. - initial API and implementation and/or initial documentation
 *		Zend Technologies Ltd. - initial implementation
 *		IBM Corporation - initial API and implementation
 *******************************************************************************/  

package org.eclipse.thym.win.internal.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.HybridProjectLaunchConfigConstants;
import org.eclipse.thym.win.core.WinCore;
import org.eclipse.thym.win.core.build.WinBuild;
import org.eclipse.thym.win.core.build.WinConstants;
import org.eclipse.thym.core.internal.cordova.CordovaCLI;

public class WinLaunchDelegate implements ILaunchConfigurationDelegate2 {

	private static String EMULATE_ARGS = "windows --archs=\"x86\" -- -phone";
	private static String SIM_ARGS = "windows --archs=\"x86\" -- -win";
	private static String RUN_ARGS = "windows --device --archs=\"arm\" -- -phone";
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
	    // Set up
	    monitor.beginTask(Messages.WinLaunchDelegate_LaunchEmulator, 10);
	    IProject kernelProject = getProject(configuration);
	    Assert.isNotNull(kernelProject,
	        Messages.WinLaunchDelegate_NoProjectError);
	
	    HybridProject project = HybridProject.getHybridProject(kernelProject);
	    if (project == null) {
	      throw new CoreException(new Status(IStatus.ERROR, WinCore.PLUGIN_ID,
	            NLS.bind(Messages.WinLaunchDelegate_NotHybridError,
	              kernelProject.getName())));
	    }
	    
	    SubMonitor sm = SubMonitor.convert(monitor,100);
	
	    // Determine which way to run Win Universal
		String launchType = configuration.getAttribute(WinConstants.ATTR_LAUNCH_TYPE, (String)null);
		if (launchType.equals(WinConstants.ATTR_LAUNCH_TYPE_EMULATOR))
			CordovaCLI.newCLIforProject(project).emulate(sm.newChild(90), EMULATE_ARGS);
		else if (launchType.equals(WinConstants.ATTR_LAUNCH_TYPE_SIMULATOR))
			CordovaCLI.newCLIforProject(project).emulate(sm.newChild(90), SIM_ARGS);
		else if (launchType.equals(WinConstants.ATTR_LAUNCH_TYPE_DEVICE))
			CordovaCLI.newCLIforProject(project).run(sm.newChild(90), RUN_ARGS);
	
	    sm.worked(30);
	    monitor.worked(2);
	    monitor.done();
	}

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
			throws CoreException {
		return null;
	}

	@SuppressWarnings("restriction")
	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		WinBuild build = new WinBuild();
		build.init(getProject(configuration), null);
		build.buildNow(monitor);
		return false;
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		monitor.done();
		return true;
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) {
		return true;
	}

	private IProject getProject(ILaunchConfiguration configuration) {
		try {
			String projectName = configuration.getAttribute(
					HybridProjectLaunchConfigConstants.ATTR_BUILD_SCOPE,
					(String) null);
			if (projectName != null) {
				return ResourcesPlugin.getWorkspace().getRoot()
						.getProject(projectName);
			}
		} catch (CoreException e) {
			return null;
		}
		return null;
	}

}
