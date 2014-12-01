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
package org.eclipse.thym.wp.core.vstudio;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.internal.util.ExternalProcessUtility;
import org.eclipse.thym.core.internal.util.TextDetectingStreamListener;
import org.eclipse.thym.core.platform.AbstractNativeBinaryBuildDelegate;
import org.eclipse.thym.wp.core.WPCore;
import org.eclipse.thym.wp.internal.core.Messages;
import org.eclipse.thym.wp.internal.core.Version;
import org.eclipse.thym.wp.internal.core.WindowsRegistry;
import org.eclipse.thym.wp.internal.core.vstudio.WPProjectGenerator;
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
	private static final String WP_CORDOVA_CLASS_LIB_DLL = "WPCordovaClassLib.dll"; //$NON-NLS-1$
	
	private static final String INSTALL_ROOT = "InstallRoot"; //$NON-NLS-1$
	private static final String DOT_NET = "HKLM\\Software\\Wow6432Node\\Microsoft\\.NETFramework"; //$NON-NLS-1$

	private ILaunchConfiguration launchConfiguration;

	public void buildLibraryProject(File projectLocation,
			IProgressMonitor monitor) throws CoreException {
		doBuildProject(projectLocation, monitor);
		setBuildArtifact(new File(getBuildDir(projectLocation),
				WP_CORDOVA_CLASS_LIB_DLL));
		if (!getBuildArtifact().exists()) {
			throw new CoreException(new Status(IStatus.ERROR, WPCore.PLUGIN_ID,
					Messages.MSBuild_MSBuildFailedMessage));
		}
	}

	@Override
	public void buildNow(IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			return;
		}
		try {
			monitor.beginTask(Messages.MSBuild_BuildProjectTask, 10);
			// TODO: use extension point to create the generator.
			WPProjectGenerator creator = new WPProjectGenerator(getProject(),
					null, WPProjectUtils.WP8);
			SubProgressMonitor generateMonitor = new SubProgressMonitor(
					monitor, 1);
			File vstudioProjectDir = creator.generateNow(generateMonitor);

			monitor.worked(4);
			if (monitor.isCanceled()) {
				return;
			}
			doBuildProject(vstudioProjectDir, generateMonitor);
			HybridProject hybridProject = HybridProject.getHybridProject(this
					.getProject());
			if (hybridProject == null) {
				throw new CoreException(new Status(IStatus.ERROR,
						WPCore.PLUGIN_ID, Messages.MSBuild_NoHybridError));
			}
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

	private void doBuildProject(File projectLocation, IProgressMonitor monitor)
			throws CoreException {
		if (monitor.isCanceled()) {
			return;
		}
		try {
			monitor.beginTask(Messages.MSBuild_BuildProjectTask, 10);
			String msBuild = getMSBuildPath();
			if (msBuild != null) {
				File csprojFile = WPProjectUtils.getCsrojFile(projectLocation);
				// on this stage it cannot be null
				Assert.isNotNull(csprojFile);
				StringBuilder cmdString = new StringBuilder(msBuild);
				cmdString.append(" "); //$NON-NLS-1$
				if (isRelease()) {
					cmdString.append("/p:Configuration=Release "); //$NON-NLS-1$
				}
				cmdString.append(csprojFile.getAbsolutePath());

				ExternalProcessUtility processUtility = new ExternalProcessUtility();
				if (monitor.isCanceled()) {
					return;
				}
				monitor.worked(1);
				TextDetectingStreamListener listener = new TextDetectingStreamListener(
						"Build succeeded."); //$NON-NLS-1$
				processUtility.execSync(cmdString.toString(), projectLocation,
						listener, listener, monitor, null,
						getLaunchConfiguration());
				if (!listener.isTextDetected()) {
					throw new CoreException(new Status(IStatus.ERROR,
							WPCore.PLUGIN_ID, Messages.MSBuild_MSBuildError));
				}
			}
		} finally {
			monitor.done();
		}
	}

	private String getInstallationRoot() throws CoreException {
		return WindowsRegistry.readRegistry(DOT_NET, INSTALL_ROOT);
	}

}
