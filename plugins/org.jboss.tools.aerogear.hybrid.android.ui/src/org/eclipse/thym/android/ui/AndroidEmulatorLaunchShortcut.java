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
package org.eclipse.thym.android.ui;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.thym.android.core.adt.AndroidLaunchConstants;
import org.eclipse.thym.android.core.adt.AndroidSDK;
import org.eclipse.thym.android.core.adt.AndroidSDKManager;
import org.eclipse.thym.ui.launch.HybridProjectLaunchShortcut;

public class AndroidEmulatorLaunchShortcut extends HybridProjectLaunchShortcut {

	@Override
	protected boolean validateBuildToolsReady() throws CoreException {
		
		AndroidSDKManager sdkManager = AndroidSDKManager.getManager();
		List<AndroidSDK> targets = sdkManager.listTargets();
		if(targets == null || targets.isEmpty() ){
			throw new CoreException(new Status(IStatus.ERROR, AndroidUI.PLUGIN_ID, "No targets to build against"));
		}
		return true;
	}

	@Override
	protected String getLaunchConfigurationTypeID() {
		return AndroidLaunchConstants.ID_LAUNCH_CONFIG_TYPE;
	}
	
	@Override
	protected void updateLaunchConfiguration(ILaunchConfigurationWorkingCopy wc) {
		wc.setAttribute(AndroidLaunchConstants.ATTR_IS_DEVICE_LAUNCH, false);
		super.updateLaunchConfiguration(wc);
	}
	
	@Override
	protected boolean isCorrectLaunchConfiguration(IProject project,
			ILaunchConfiguration config) throws CoreException {
		if(config.getAttribute(AndroidLaunchConstants.ATTR_IS_DEVICE_LAUNCH, false)){
			return false;
		}
		return super.isCorrectLaunchConfiguration(project, config);
	}

	@Override
	protected String getLaunchConfigurationNamePrefix(IProject project) {
		return project.getName()+ " (Android Emulator)";
	}

}
