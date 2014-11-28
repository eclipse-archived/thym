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
package org.eclipse.thym.ios.ui;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridMobileStatus;
import org.eclipse.thym.ios.core.IOSCore;
import org.eclipse.thym.ios.core.simulator.IOSSimulatorLaunchConstants;
import org.eclipse.thym.ios.core.xcode.XCodeBuild;
import org.eclipse.thym.ios.core.xcode.XCodeSDK;
import org.eclipse.thym.ui.launch.HybridProjectLaunchShortcut;
import org.osgi.framework.Version;
/**
 * Launch shortcut for launching iOS Simulator. 
 * @see HybridProjectLaunchShortcut
 * 
 * @author Gorkem Ercan
 *
 */
public class IOSLaunchShortcut extends HybridProjectLaunchShortcut{

	@Override
	protected boolean validateBuildToolsReady() throws CoreException {
		XCodeBuild xcode = new XCodeBuild();
		String version = xcode.version();
		if ( version == null ){
			throw new CoreException(new Status(IStatus.ERROR, IOSUI.PLUGIN_ID, "Can not retrieve xcode version, is xcode properly installed?"));
		}
		try{
			Version minVersion = new Version(XCodeBuild.MIN_REQUIRED_VERSION);
			Version v = Version.parseVersion(version);
			if(v.compareTo(minVersion)<0){
				throw new CoreException(new HybridMobileStatus(IStatus.ERROR, IOSCore.PLUGIN_ID, 300/*see org.eclipse.thym.ios.ui.xcodeVersionStatusHandler in plugin.xml*/,
						NLS.bind("Hybrid mobile projects require XCode version {0} or greater to build iOS applications", XCodeBuild.MIN_REQUIRED_VERSION),null));
			}
		}catch (IllegalArgumentException e) {
			//ignored
		}
		
		List <XCodeSDK> sdks = xcode.showSdks();
		boolean iosSdkAvailable = false;
		for (XCodeSDK xcodeSDK : sdks) {
			if(xcodeSDK.isIOS()){
				iosSdkAvailable =true;
				break;
			}
		}
		if(!iosSdkAvailable){
			throw new CoreException(new Status(IStatus.ERROR, IOSUI.PLUGIN_ID, "No iOS SDKs are found. Please install an iOS SDK and try again."));
		}
		
		return true;
	}

	@Override
	protected String getLaunchConfigurationTypeID() {
		return IOSSimulatorLaunchConstants.ID_LAUNCH_CONFIG_TYPE;
	}

	@Override
	protected String getLaunchConfigurationNamePrefix(IProject project) {
		return project.getName() + " (iOS Simulator)";
	}

}
