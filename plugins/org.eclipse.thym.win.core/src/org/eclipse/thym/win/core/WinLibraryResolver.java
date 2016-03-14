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

package org.eclipse.thym.win.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.HybridMobileLibraryResolver;
import org.eclipse.thym.core.internal.util.FileUtils;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.win.internal.core.Messages;

/**
 * Implementation of {@link HybridMobileLibraryResolver} for Windows Universal
 * platform.
 * 
 * @author Wojciech Galanciak, James Dubee 2014, 2016
 * 
 */
public class WinLibraryResolver extends HybridMobileLibraryResolver {

	private static final String WP8 = "windows"; //$NON-NLS-1$
	public static final String VERSION = "VERSION"; //$NON-NLS-1$
	public static final String WP_APP_MANIFEST_XML = "WMAppManifest.xml"; //$NON-NLS-1$
	public static final String APP_XAML = "App.xaml"; //$NON-NLS-1$
	public static final String APP_XAML_CS = "App.xaml.cs"; //$NON-NLS-1$
	public static final String MAIN_PAGE_XAML = "MainPage.xaml"; //$NON-NLS-1$
	public static final String MAIN_PAGE_XAML_CS = "MainPage.xaml.cs"; //$NON-NLS-1$
	public static final String DEFAULT_APP_NAME = "CordovaWP8AppProj"; //$NON-NLS-1$
	public static final String DEFAULT_SLN_NAME = "CordovaWP8Solution.sln"; //$NON-NLS-1$

	private static final String TEMPLATE = "template"; //$NON-NLS-1$
	private static final String PROPERTIES = "Properties"; //$NON-NLS-1$

	private HashMap<IPath, URL> files = new HashMap<IPath, URL>();

	@Override
	public URL getTemplateFile(IPath destination) {
		Assert.isNotNull(destination);
		Assert.isTrue(!destination.isAbsolute());
		return files.get(destination);
	}

	@Override
	public IStatus isLibraryConsistent() {
		if (version == null) {
			return new Status(IStatus.ERROR, HybridCore.PLUGIN_ID,
					Messages.WPLibraryResolver_NotCompatibleError);
		}

		return libraryRoot.lastSegment().equals(version) ? Status.OK_STATUS
				: Status.CANCEL_STATUS;
	}

	@Override
	public void preCompile(IProgressMonitor monitor) throws CoreException {
	}

	@Override
	public boolean needsPreCompilation() {
		return false;
	}

	@Override
	public String detectVersion() {
		File versionFile = this.libraryRoot.append(VERSION).toFile();
		if (versionFile.exists()) {
			BufferedReader reader = null;
			try {
				try {
					reader = new BufferedReader(new FileReader(versionFile));
					String version = reader.readLine();
					if (version != null) {
						return version.trim();
					}
				} finally {
					if (reader != null)
						reader.close();
				}
			} catch (IOException e) {
				WinCore.log(IStatus.ERROR,
						Messages.WPLibraryResolver_CannotDetectError, e);
			}
		} else {
			WinCore.log(IStatus.ERROR, NLS.bind(
					Messages.WPLibraryResolver_NoVersionError,
					versionFile.toString()), null);
		}
		return null;
	}
}
