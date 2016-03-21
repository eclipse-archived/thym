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
import java.util.Iterator;

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
import org.eclipse.thym.win.core.WinCore;

public class WinLibraryResolver extends HybridMobileLibraryResolver {

	private static final String WIN = "windows"; //$NON-NLS-1$
	public static final String VERSION = "VERSION"; //$NON-NLS-1$
	public static final String WIN_PHONE_APP_MANIFEST = "package.phone.appxmanifest"; //$NON-NLS-1$
	public static final String WIN_APP_MANIFEST = "package.windows.appxmanifest"; //$NON-NLS-1$
	public static final String WIN_10_APP_MANIFEST = "package.windows10.appxmanifest"; //$NON-NLS-1$
	public static final String WIN_80_APP_MANIFEST = "package.windows80.appxmanifest"; //$NON-NLS-1$
	
	public static final String WIN_JSPROJ = "CordovaApp.Windows.jsproj"; //$NON-NLS-1$
	public static final String WIN_PHONE_JSPROJ = "CordovaApp.Phone.jsproj"; //$NON-NLS-1$
	public static final String WIN_10_JSPROJ = "CordovaApp.Windows10.jsproj"; //$NON-NLS-1$
	public static final String WIN_80_JSPROJ = "CordovaApp.Windows80.jsproj"; //$NON-NLS-1$

	public static final String WIN_APP_NAME = "CordovaApp"; //$NON-NLS-1$
	public static final String WIN_SLN_NAME = "CordovaApp.sln"; //$NON-NLS-1$

	private static final String TEMPLATE = "template"; //$NON-NLS-1$
	private static final String PROPERTIES = "Properties"; //$NON-NLS-1$

	private HashMap<IPath, URL> files = new HashMap<IPath, URL>();

	private void initFiles() {
		IPath templatePrjRoot = libraryRoot.append(TEMPLATE);
		files.put(PATH_CORDOVA_JS,
				getEngineFile(templatePrjRoot.append(PlatformConstants.DIR_WWW)
						.append(PlatformConstants.FILE_JS_CORDOVA)));
		files.put(new Path(VAR_APP_NAME), getEngineFile(templatePrjRoot));
		files.put(new Path(VERSION), getEngineFile(libraryRoot.append(VERSION)));
		
		files.put(new Path(WIN_PHONE_APP_MANIFEST), 
				getEngineFile(templatePrjRoot.append(WIN_PHONE_APP_MANIFEST)));
		files.put(new Path(WIN_APP_MANIFEST), 
				getEngineFile(templatePrjRoot.append(WIN_APP_MANIFEST)));
		files.put(new Path(WIN_10_APP_MANIFEST), 
				getEngineFile(templatePrjRoot.append(WIN_10_APP_MANIFEST)));
		files.put(new Path(WIN_80_APP_MANIFEST), 
				getEngineFile(templatePrjRoot.append(WIN_80_APP_MANIFEST)));
		
		files.put(new Path(WIN_JSPROJ),
				getEngineFile(templatePrjRoot.append(WIN_JSPROJ)));
		files.put(new Path(WIN_PHONE_JSPROJ),
				getEngineFile(templatePrjRoot.append(WIN_PHONE_JSPROJ)));
		files.put(new Path(WIN_10_JSPROJ),
				getEngineFile(templatePrjRoot.append(WIN_10_JSPROJ)));
		files.put(new Path(WIN_80_JSPROJ),
				getEngineFile(templatePrjRoot.append(WIN_80_JSPROJ)));
		
		files.put(new Path(WIN_SLN_NAME),
				getEngineFile(templatePrjRoot.append(WIN_SLN_NAME)));
	}
	
	private URL getEngineFile(IPath path) {
		File file = path.toFile();
		if (!file.exists()) {
			WinCore.log(IStatus.WARNING, NLS.bind(
					Messages.WinLibraryResolver_MissingEngineError,
					file.toString()), null);
		}
		
		return FileUtils.toURL(file);
	}
	
	@Override
	public IStatus isLibraryConsistent() {
		if(version == null ){
			return new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Library for Android platform is not compatible with this tool. File for path {0} is missing.");
		}
		if(files.isEmpty()) initFiles();
		Iterator<IPath> paths = files.keySet().iterator();
		while (paths.hasNext()) {
			IPath key = paths.next();
			URL url = files.get(key);
			if(url != null  ){
				File file = new File(url.getFile());
				if( file.exists())
					continue;
			}
			return new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, NLS.bind("Library for Windows Universal platform is not compatible with this tool. File for path {0} is missing.", key.toString()));
		}
		return Status.OK_STATUS;
	}
	
	@Override
	public URL getTemplateFile(IPath destination) {
		if(files.isEmpty()) initFiles();
		Assert.isNotNull(destination);
		Assert.isTrue(!destination.isAbsolute());
		return files.get(destination);
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
						Messages.WinLibraryResolver_CannotDetectError, e);
			}
		} else {
			WinCore.log(IStatus.ERROR, NLS.bind(
					Messages.WinLibraryResolver_NoVersionError,
					versionFile.toString()), null);
		}
		return null;
	}
}
