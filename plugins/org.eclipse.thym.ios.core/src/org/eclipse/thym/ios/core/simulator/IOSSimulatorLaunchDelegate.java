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
package org.eclipse.thym.ios.core.simulator;

import static org.eclipse.thym.ios.core.simulator.IOSSimulatorLaunchConstants.ATTR_DEVICE_FAMILY;
import static org.eclipse.thym.ios.core.simulator.IOSSimulatorLaunchConstants.ATTR_USE_64BIT;
import static org.eclipse.thym.ios.core.simulator.IOSSimulatorLaunchConstants.ATTR_USE_RETINA;
import static org.eclipse.thym.ios.core.simulator.IOSSimulatorLaunchConstants.ATTR_USE_TALL;
import static org.eclipse.thym.ios.core.simulator.IOSSimulatorLaunchConstants.VAL_DEVICE_FAMILY_IPHONE;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.ios.core.IOSCore;
import org.eclipse.thym.ios.core.xcode.XCodeBuild;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProjectLaunchConfigConstants;

import com.github.zafarkhaja.semver.Version;
import com.github.zafarkhaja.semver.util.UnexpectedElementException;
/**
 * {@link ILaunchDelegate} for running the iOS simulator. This delegate is unusual 
 * because besides running the emulator it also generates and builds the cordova project.
 * 
 * @author Gorkem Ercan
 *
 */
public class IOSSimulatorLaunchDelegate implements
		ILaunchConfigurationDelegate2 {
	private static Version MIN_VERSION = Version.valueOf(XCodeBuild.MIN_REQUIRED_VERSION);

	private File buildArtifact;
	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		monitor.beginTask("Launch iOS Simulator", 10);
		IOSSimulator simulator = new IOSSimulator();
		IProject project = getProject(configuration);
		Assert.isNotNull(project, "Can not launch with a null project");
		simulator.setPathToBinary(buildArtifact.getPath());
		simulator.setFamily(configuration.getAttribute(ATTR_DEVICE_FAMILY, VAL_DEVICE_FAMILY_IPHONE));
		simulator.setRetina(configuration.getAttribute(ATTR_USE_RETINA, false));
		simulator.setTall(configuration.getAttribute(ATTR_USE_TALL, false));
		simulator.set64bit(configuration.getAttribute(ATTR_USE_64BIT, false));
	
		String[] envp = DebugPlugin.getDefault().getLaunchManager()
				.getEnvironment(configuration);
		simulator.setProcessEnvironmentVariables(envp);
		monitor.worked(2);
		simulator.launch();
		monitor.done();
	}

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
			throws CoreException {
		return null;
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		
		XCodeBuild build = new XCodeBuild();
		build.setLaunchConfiguration(configuration);
		build.init(getProject(configuration), null);
		build.buildNow(monitor);
		buildArtifact = build.getBuildArtifact();
		
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
			String mode, IProgressMonitor monitor) throws CoreException {
		
		XCodeBuild xcode = new XCodeBuild();
		String version = xcode.version();
		try{
			Version v = Version.valueOf(version);
			if(v.lessThan(MIN_VERSION)){
				throw new CoreException(new Status(IStatus.ERROR, IOSCore.PLUGIN_ID,
						NLS.bind("Hybrid mobile projects can only be run with XCode version {0} or greater", XCodeBuild.MIN_REQUIRED_VERSION )));
			}
		}catch (UnexpectedElementException e) {
			//We could not parse the version
			//still let the build continue 
			HybridCore.log(IStatus.WARNING, "Error parsing the xcode version. Version String is "+ version, e);
			return true;
		}

 		monitor.done();
		return true;
	}
	
	private IProject getProject(ILaunchConfiguration configuration){
		try{
			String projectName = configuration.getAttribute(HybridProjectLaunchConfigConstants.ATTR_BUILD_SCOPE, (String)null);
			if(projectName != null ){
				 return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			}
		}catch(CoreException e){
			return null;
		}
		return null;
	}

}
