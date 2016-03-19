/*******************************************************************************
 * Copyright (c) 2014, 2016 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 * 		 Zend Technologies Ltd. - initial implementation
 *******************************************************************************/
package org.eclipse.thym.wp.core.vstudio;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.internal.cordova.CordovaCLI;
import org.eclipse.thym.core.internal.cordova.ErrorDetectingCLIResult;
import org.eclipse.thym.core.platform.AbstractNativeBinaryBuildDelegate;
import org.eclipse.thym.wp.core.WPCore;
import org.eclipse.thym.wp.internal.core.Messages;
import org.eclipse.thym.wp.internal.core.Version;
import org.eclipse.thym.wp.internal.core.WindowsRegistry;
import org.eclipse.thym.wp.internal.core.vstudio.WPProjectUtils;

/**
 * Wrapper for MSBuild tool on Windows.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */
@SuppressWarnings("restriction")
public class MSBuild extends AbstractNativeBinaryBuildDelegate {

	private static final String DEBUG_XAP_NAME = "CordovaAppProj_Debug_AnyCPU.xap"; //$NON-NLS-1$
	private static final String RELEASE_XAP_NAME = "CordovaAppProj_Release_AnyCPU.xap"; //$NON-NLS-1$
	
	private static final String INSTALL_ROOT = "InstallRoot"; //$NON-NLS-1$
	private static final String DOT_NET = "HKLM\\Software\\Wow6432Node\\Microsoft\\.NETFramework"; //$NON-NLS-1$

	private ILaunchConfiguration launchConfiguration;


	@Override
	public void buildNow(IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			return;
		}
		try {
			monitor.beginTask(Messages.MSBuild_BuildProjectTask, 10);
			SubMonitor generateMonitor =SubMonitor.convert(monitor,5);
			if (monitor.isCanceled()) {
				return;
			}
			HybridProject hybridProject = HybridProject.getHybridProject(this
					.getProject());
			if (hybridProject == null) {
				throw new CoreException(new Status(IStatus.ERROR,
						WPCore.PLUGIN_ID, Messages.MSBuild_NoHybridError));
			}
			String buildType = "--debug";
			if(isRelease()){
				buildType = "--release";
			}
			IStatus status = 
			CordovaCLI.newCLIforProject(hybridProject).build(generateMonitor, WPProjectUtils.WP8, buildType).convertTo(ErrorDetectingCLIResult.class).asStatus();
			this.getProject().refreshLocal(IResource.DEPTH_INFINITE, generateMonitor);
			if(status.getSeverity() == IStatus.ERROR){
				throw new CoreException(status);
			}
			
			File vstudioProjectDir = hybridProject.getProject().getFolder("platforms/wp8").getLocation().toFile();
			if (isRelease()) {
				setBuildArtifact(new File(getBuildDir(vstudioProjectDir),
						RELEASE_XAP_NAME));
			} else {
				setBuildArtifact(new File(getBuildDir(vstudioProjectDir),
						DEBUG_XAP_NAME));
			}
		} finally {
			monitor.done();
		}
	}

	public void setLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
		this.launchConfiguration = launchConfiguration;
	}

	public ILaunchConfiguration getLaunchConfiguration() {
		return launchConfiguration;
	}

	/**
	 * Get absolute path to MSBuild executable. It is detected base on Windows
	 * Registry.
	 * 
	 * @return path to MSBuild executable
	 * @throws CoreException 
	 */
	public String getMSBuildPath() throws CoreException {
		String installationRoot = getInstallationRoot();
		if (installationRoot != null) {
			File installationFile = new File(installationRoot);
			if (installationFile.exists()) {
				File[] versionFiles = installationFile
						.listFiles(new FileFilter() {

							@Override
							public boolean accept(File pathname) {
								return pathname.getName().startsWith("v"); //$NON-NLS-1$
							}
						});
				File highestVersion = null;
				for (File file : versionFiles) {
					if (highestVersion == null) {
						highestVersion = file;
					} else {
						Version current = Version.byName(highestVersion
								.getName());
						Version newOne = Version.byName(file.getName());
						if (newOne.compareTo(current) > 0) {
							highestVersion = file;
						}
					}
				}
				if (highestVersion != null) {
					return new File(highestVersion.getAbsolutePath(),
							WPConstants.MS_BUILD).getAbsolutePath();
				}
			}
		}
		return null;
	}

	/**
	 * Returns the actual folder where the build artifacts can be found.
	 * 
	 * @param vstudioProjectFolder
	 *            - Visual Studio project's root folder
	 * @return folder with the build artifacts
	 */
	private File getBuildDir(File vstudioProjectFolder) {
		File binFolder = new File(vstudioProjectFolder, WPProjectUtils.BIN);
		return new File(binFolder, isRelease() ? WPProjectUtils.RELEASE
				: WPProjectUtils.DEBUG);
	}

	private String getInstallationRoot() throws CoreException {
		return WindowsRegistry.readRegistry(DOT_NET, INSTALL_ROOT);
	}
	

}
