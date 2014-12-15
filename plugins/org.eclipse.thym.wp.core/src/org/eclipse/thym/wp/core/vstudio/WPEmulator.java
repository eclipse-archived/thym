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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.thym.core.internal.util.ExternalProcessUtility;
import org.eclipse.thym.wp.core.WPCore;
import org.eclipse.thym.wp.internal.core.Messages;

/**
 * Wrapper of AppDeployCmd and XapDeployCmd responsible for deploying and
 * launching Windows Phone 8 applications on emulator.
 * 
 * @author Wojciech Galanciak, 2014
 *
 */
@SuppressWarnings("restriction")
public class WPEmulator {

	private static final String APP_DEPLOY_CMD_EXE = "AppDeployCmd.exe"; //$NON-NLS-1$
	private static final String APP_DEPLOY = "AppDeploy"; //$NON-NLS-1$
	private static final String XAP_DEPLOY_CMD_EXE = "XapDeployCmd.exe"; //$NON-NLS-1$
	private static final String XAP_DEPLOYMENT = "XAP Deployment"; //$NON-NLS-1$
	private static final String TOOLS = "Tools"; //$NON-NLS-1$

	private static class DeviceListParser implements IStreamListener {

		private StringBuffer buffer = new StringBuffer();

		@Override
		public void streamAppended(String text, IStreamMonitor monitor) {
			buffer.append(text);
		}

		public Map<String, Integer> parseDevices() {
			if (buffer == null || buffer.length() < 1)
				return null;
			Map<String, Integer> devices = new TreeMap<String, Integer>();
			try {
				StringReader reader = new StringReader(buffer.toString());
				BufferedReader read = new BufferedReader(reader);
				String line = null;

				while ((line = read.readLine()) != null) {
					if (line.isEmpty())
						continue;
					if (line.equals("Done.")) { //$NON-NLS-1$
						break;
					}
					String[] segments = line.split("\t\t"); //$NON-NLS-1$
					try {
						int index = Integer.valueOf(segments[0].trim());
						String name = segments[1].trim();
						if (!"Device".equals(name)) { //$NON-NLS-1$
							devices.put(name, index);
						}
					} catch (NumberFormatException e) {
						// skip and continue
					}
				}
			} catch (IOException e) {
				WPCore.log(IStatus.ERROR,
						"Error during listing Windows Phone 8 devices", e); //$NON-NLS-1$
			}
			return devices;
		}

	}

	private String sdkLocation;
	private String[] environment;
	private IProgressMonitor monitor;

	public WPEmulator(String sdkLocation) {
		this.sdkLocation = sdkLocation;
	}

	/**
	 * Emulate specified Windows Phone application on selected device.
	 * 
	 * @param xapFile
	 *            deployable Windows Phone package
	 * @param deviceId
	 *            device's id
	 * @throws CoreException
	 */
	public void emulate(File xapFile, int deviceId) throws CoreException {
		if (sdkLocation == null) {
			sdkLocation = WPCore.getSDKLocation();
			if (sdkLocation == null) {
				return;
			}
		}
		File deployCmd = getDeployCommand();
		installLaunch(deployCmd, xapFile, deviceId);
	}

	/**
	 * Get map (id -> name) of local Windows Phone 8 devices. It performs
	 * following command:
	 * <p>
	 * <code>AppDeployCmd/XapDeployCmd.exe /EnumerateDevices</code>
	 * </p>
	 * 
	 * @return map of local Windows Phone 8 devices or <code>null</code> if they
	 *         are not available
	 * @throws CoreException
	 */
	public Map<String, Integer> getDevices() throws CoreException {
		if (sdkLocation == null) {
			sdkLocation = WPCore.getSDKLocation();
			if (sdkLocation == null) {
				return null;
			}
		}
		File deployCmd = getDeployCommand();
		StringBuilder cmdLine = new StringBuilder();
		cmdLine.append(addQuotes(deployCmd.getAbsolutePath()));
		cmdLine.append(" /EnumerateDevices"); //$NON-NLS-1$
		ExternalProcessUtility processUtility = new ExternalProcessUtility();
		DeviceListParser parser = new DeviceListParser();
		processUtility.execSync(cmdLine.toString(), null, parser, parser,
				monitor, null, null);
		return parser.parseDevices();
	}

	/**
	 * The environment variables set in the process
	 * 
	 * @param envp
	 */
	public WPEmulator setProcessEnvironmentVariables(String[] envp) {
		this.environment = envp;
		return this;
	}

	public WPEmulator setProgressMonitor(IProgressMonitor progressMonitor) {
		this.monitor = progressMonitor;
		return this;
	}

	/**
	 * Get Windows Phone 8 deployment command line tool.
	 * 
	 * @return XapDeployCmd or AppDeployCmd file or <code>null</code> if they do
	 *         not exist
	 * @throws CoreException
	 */
	private File getDeployCommand() throws CoreException {
		File deployCmd = getAppDeployCmd();
		if (deployCmd == null) {
			deployCmd = getXapDeployCmd();
			if (deployCmd == null) {
				throw new CoreException(new Status(IStatus.ERROR,
						WPCore.PLUGIN_ID, Messages.WPEmulator_NoDeployCmdError));
			}
		}
		return deployCmd;
	}

	private IProgressMonitor getProgressMonitor() {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		return monitor;
	}

	/**
	 * Install and launch specified XAP package on selected device. It performs
	 * following command:
	 * <p>
	 * <code>AppDeployCmd/XapDeployCmd.exe /installlaunch xapFile /targetdevice:deviceId</code>
	 * </p>
	 * 
	 * @param deployCmd
	 *            XapDeployCmd.exe or AppDeployCmd.exe file
	 * @param xapFile
	 *            XAP file
	 * @param deviceId
	 *            device's id
	 * @throws CoreException
	 */
	private void installLaunch(File deployCmd, File xapFile, int deviceId)
			throws CoreException {
		StringBuilder cmdLine = new StringBuilder();
		cmdLine.append(addQuotes(deployCmd.getAbsolutePath()));
		cmdLine.append(" /installlaunch "); //$NON-NLS-1$
		cmdLine.append(addQuotes(xapFile.getAbsolutePath()));
		cmdLine.append(" /targetdevice:"); //$NON-NLS-1$
		if (deviceId == -1) {
			// set default emulator id
			cmdLine.append("xd"); //$NON-NLS-1$
		} else {
			cmdLine.append(deviceId);
		}
		ExternalProcessUtility processUtility = new ExternalProcessUtility();
		processUtility.execSync(cmdLine.toString(), null, null, null,
				getProgressMonitor(), environment, null);
	}

	/**
	 * Check if "<sdk_root>\Tools\XAP Deployment\XapDeployCmd.exe" file exists.
	 * 
	 * @return XapDeployCmd file or <code>null</code> if it does not exist
	 */
	private File getXapDeployCmd() {
		File xapDeloyCmd = new File(sdkLocation, TOOLS + File.separator
				+ XAP_DEPLOYMENT + File.separator + XAP_DEPLOY_CMD_EXE);
		return xapDeloyCmd.exists() ? xapDeloyCmd : null;
	}

	/**
	 * Check if "<sdk_root>\Tools\AppDeploy\AppDeployCmd.exe" file exists.
	 * 
	 * @return AppDeployCmd file or <code>null</code> if it does not exist
	 */
	private File getAppDeployCmd() {
		File appDeloyCmd = new File(sdkLocation, TOOLS + File.separator
				+ APP_DEPLOY + File.separator + APP_DEPLOY_CMD_EXE);
		return appDeloyCmd.exists() ? appDeloyCmd : null;
	}

	private String addQuotes(String path) {
		return "\"" + path + "\""; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
