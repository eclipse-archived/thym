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
package org.eclipse.thym.win.core;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.thym.win.core.vstudio.WPConstants;
import org.eclipse.thym.win.internal.core.WindowsRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Wojciech Galanciak, 2014
 *
 */
public class WPCore implements BundleActivator {

	public static final String WP_LAUNCH_ID = "org.eclipse.thym.win.core.WPLaunchConfigurationType"; //$NON-NLS-1$

	public static final String PLUGIN_ID = "org.eclipse.thym.win.core"; //$NON-NLS-1$

	private static final String THYM_UI_ID = "org.eclipse.thym.ui"; //$NON-NLS-1$

	private static final String WINDOWS_PHONE_REG = "HKLM\\Software\\Wow6432Node\\Microsoft\\Microsoft SDKs\\WindowsPhone"; //$NON-NLS-1$
	private static final String INSTALL_PATH = "Install Path"; //$NON-NLS-1$
	
	private static BundleContext context;

	private static ILog logger;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		WPCore.context = bundleContext;
		logger = Platform.getLog(getContext().getBundle());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		WPCore.context = null;
	}

	public static void log(int status, String message, Throwable throwable) {
		logger.log(new Status(status, PLUGIN_ID, message, throwable));
	}

	/**
	 * Get path to Windows Phone SDK root folder.
	 * 
	 * @return path to Windows Phone SDK root folder or <code>null</code> if SDK
	 *         cannot be retrieved from preferences or detected from Windows
	 *         registry.
	 * @throws CoreException 
	 */
	public static String getSDKLocation() throws CoreException {
		String sdkLocation = Platform.getPreferencesService().getString(
				THYM_UI_ID, WPConstants.WINDOWS_PHONE_SDK_LOCATION_PREF, null,
				null);
		if (sdkLocation == null) {
			File location = detectSDK();
			if (location != null && location.exists()) {
				sdkLocation = location.getAbsolutePath();
				IEclipsePreferences prefs = InstanceScope.INSTANCE
						.getNode(THYM_UI_ID);
				prefs.put(WPConstants.WINDOWS_PHONE_SDK_LOCATION_PREF,
						sdkLocation);
			}
		}
		return sdkLocation;
	}

	private static File detectSDK() throws CoreException {
		String[] versions = WindowsRegistry.getChildren(WINDOWS_PHONE_REG);
		if (versions.length > 0) {
			Arrays.sort(versions);
			String installPath = null;
			for (int i = versions.length - 1; i >= 0; i--) {
				installPath = WindowsRegistry.readRegistry(
						WINDOWS_PHONE_REG + "\\" + versions[i] + "\\" + INSTALL_PATH, //$NON-NLS-1$ //$NON-NLS-2$
						INSTALL_PATH);
				if (installPath != null) {
					break;
				}
			}
			if (installPath != null) {
				return new File(installPath);
			}
		}
		return null;
	}

}
