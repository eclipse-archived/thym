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
package org.eclipse.thym.wp.core;

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
import org.eclipse.thym.wp.internal.core.Messages;
import org.eclipse.thym.wp.internal.core.vstudio.WPProjectUtils;

/**
 * Implementation of {@link HybridMobileLibraryResolver} for Windows Phone 8
 * platform.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */
public class WPLibraryResolver extends HybridMobileLibraryResolver {

	private static final String WP8 = "wp8"; //$NON-NLS-1$
	public static final String VERSION = "VERSION"; //$NON-NLS-1$
	public static final String WP_APP_MANIFEST_XML = "WMAppManifest.xml"; //$NON-NLS-1$
	public static final String APP_XAML = "App.xaml"; //$NON-NLS-1$
	public static final String APP_XAML_CS = "App.xaml.cs"; //$NON-NLS-1$
	public static final String MAIN_PAGE_XAML = "MainPage.xaml"; //$NON-NLS-1$
	public static final String MAIN_PAGE_XAML_CS = "MainPage.xaml.cs"; //$NON-NLS-1$
	public static final String DEFAULT_APP_NAME = "CordovaWP8AppProj"; //$NON-NLS-1$
	public static final String DEFAULT_APP_NAME_CSPROJ = DEFAULT_APP_NAME + WPProjectUtils.CSPROJ_EXTENSION;
	public static final String DEFAULT_SLN_NAME = "CordovaWP8Solution.sln"; //$NON-NLS-1$

	private static final String TEMPLATE = "template"; //$NON-NLS-1$
	private static final String PROPERTIES = "Properties"; //$NON-NLS-1$
	
	public static final String CORDOVA_WP8 = "cordova-wp8";

	private HashMap<IPath, URL> files = new HashMap<IPath, URL>();

	@Override
	public URL getTemplateFile(IPath destination) {
		if (files.isEmpty())
			initFiles();
		Assert.isNotNull(destination);
		Assert.isTrue(!destination.isAbsolute());
		return files.get(destination);
	}

	@Override
	public IStatus isLibraryConsistent() {
		if (version != null) {
			String name = readLibraryName();
			if(name != null && name.equals(CORDOVA_WP8)){
				return Status.OK_STATUS;
			}
		}
		return new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, Messages.WPLibraryResolver_NotCompatibleError);
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
				WPCore.log(IStatus.ERROR, Messages.WPLibraryResolver_CannotDetectError, e);
			}
		} else {
			WPCore.log(IStatus.ERROR, NLS.bind(Messages.WPLibraryResolver_NoVersionError, versionFile.toString()),
					null);
		}
		return null;
	}

	private void initFiles() {
		IPath templatePrjRoot = libraryRoot.append(TEMPLATE);
		files.put(PATH_CORDOVA_JS, getEngineFile(
				templatePrjRoot.append(PlatformConstants.DIR_WWW).append(PlatformConstants.FILE_JS_CORDOVA)));
		files.put(new Path(VAR_APP_NAME), getEngineFile(templatePrjRoot));
		files.put(new Path(VERSION), getEngineFile(libraryRoot.append(VERSION)));
		files.put(new Path(WP_APP_MANIFEST_XML),
				getEngineFile(templatePrjRoot.append(PROPERTIES).append(WP_APP_MANIFEST_XML)));
		files.put(new Path(APP_XAML), getEngineFile(templatePrjRoot.append(APP_XAML)));
		files.put(new Path(APP_XAML_CS), getEngineFile(templatePrjRoot.append(APP_XAML_CS)));
		files.put(new Path(MAIN_PAGE_XAML), getEngineFile(templatePrjRoot.append(MAIN_PAGE_XAML)));
		files.put(new Path(MAIN_PAGE_XAML_CS), getEngineFile(templatePrjRoot.append(MAIN_PAGE_XAML_CS)));
		files.put(new Path(DEFAULT_APP_NAME_CSPROJ), getEngineFile(templatePrjRoot.append(DEFAULT_APP_NAME_CSPROJ)));
		files.put(new Path(DEFAULT_SLN_NAME), getEngineFile(templatePrjRoot.append(DEFAULT_SLN_NAME)));
	}

	private URL getEngineFile(IPath path) {
		File file = path.toFile();
		if (!file.exists()) {
			WPCore.log(IStatus.WARNING, NLS.bind(Messages.WPLibraryResolver_MissingEngineError, file.toString()), null);
		}
		return FileUtils.toURL(file);
	}

}
