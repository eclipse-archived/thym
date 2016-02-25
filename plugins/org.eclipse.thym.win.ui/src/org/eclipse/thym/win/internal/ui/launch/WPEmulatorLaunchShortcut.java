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
package org.eclipse.thym.win.internal.ui.launch;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.core.HybridMobileStatus;
import org.eclipse.thym.ui.launch.HybridProjectLaunchShortcut;
import org.eclipse.thym.win.core.WPCore;
import org.eclipse.thym.win.core.vstudio.MSBuild;
import org.eclipse.thym.win.core.vstudio.WPConstants;
import org.eclipse.thym.win.internal.ui.Messages;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.thym.core.internal.util.ExternalProcessUtility;
import org.eclipse.thym.core.plugin.PluginMessagesCLIResult;
import org.eclipse.thym.win.core.WPCore;

import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.internal.cordova.CordovaCLI;
import org.eclipse.thym.core.internal.cordova.CordovaCLI.Command;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.HybridProjectLaunchConfigConstants;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;


/**
 * Launch shortcut for Windows Phone 8 applications.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */
public class WPEmulatorLaunchShortcut extends HybridProjectLaunchShortcut {

	@Override
	protected boolean validateBuildToolsReady() throws CoreException {
		/*MSBuild msbuild = new MSBuild();
		String msBuildPath = msbuild.getMSBuildPath();
		if (msBuildPath != null) {
			File msBuildFile = new File(msBuildPath);
			if (!msBuildFile.exists()) {
				throw createMSBuildException();
			}
		} else {
			throw createMSBuildException();
		}*/
		
		
		
		/*IProject project = (IProject)adaptable.getAdapter(IProject.class);
		
		HybridProject hybridProject = HybridProject.getHybridProject(project);
		if (hybridProject == null) {
			throw new CoreException(new Status(IStatus.ERROR, AndroidCore.PLUGIN_ID,
					"Not a hybrid mobile project, can not generate files"));
		}
		
		CordovaCLI.newCLIforProject(hybridProject).build(sm.newChild(90),"android",buildType);
		*/
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
