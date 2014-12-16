/*******************************************************************************
 * Copyright (c) 2014 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 * 		 Zend Technologies Ltd. - initial implementation
 *******************************************************************************/
package org.eclipse.thym.wp.internal.ui.launch;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.core.HybridMobileStatus;
import org.eclipse.thym.ui.launch.HybridProjectLaunchShortcut;
import org.eclipse.thym.wp.core.WPCore;
import org.eclipse.thym.wp.core.vstudio.MSBuild;
import org.eclipse.thym.wp.core.vstudio.WPConstants;
import org.eclipse.thym.wp.internal.ui.Messages;

/**
 * Launch shortcut for Windows Phone 8 applications.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */
public class WPEmulatorLaunchShortcut extends HybridProjectLaunchShortcut {

	@Override
	protected boolean validateBuildToolsReady() throws CoreException {
		MSBuild msbuild = new MSBuild();
		String msBuildPath = msbuild.getMSBuildPath();
		if (msBuildPath != null) {
			File msBuildFile = new File(msBuildPath);
			if (!msBuildFile.exists()) {
				throw createMSBuildException();
			}
		} else {
			throw createMSBuildException();
		}
		
		return true;
	}

	@Override
	protected String getLaunchConfigurationTypeID() {
		return WPConstants.ID_LAUNCH_CONFIG_TYPE;
	}

	@Override
	protected String getLaunchConfigurationNamePrefix(IProject project) {
		return MessageFormat.format(
				Messages.WPEmulatorLaunchShortcut_DefaultName,
				project.getName());
	}

	/**
	 * @return exception for missing MSBuild
	 */
	private CoreException createMSBuildException() {
		return new CoreException(new HybridMobileStatus(IStatus.ERROR,
				WPCore.PLUGIN_ID, WPConstants.MISSING_MSBUILD_STATUS_CODE,
				Messages.WPEmulatorLaunchShortcut_MSBuildMissingMessage, null));
	}

}
