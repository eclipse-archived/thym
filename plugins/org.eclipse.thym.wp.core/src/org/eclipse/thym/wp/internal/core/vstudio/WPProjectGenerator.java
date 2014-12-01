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
package org.eclipse.thym.wp.internal.core.vstudio;

import static org.eclipse.thym.core.engine.HybridMobileLibraryResolver.VAR_APP_NAME;
import static org.eclipse.thym.core.internal.util.FileUtils.directoryCopy;
import static org.eclipse.thym.core.internal.util.FileUtils.fileCopy;
import static org.eclipse.thym.core.internal.util.FileUtils.templatedFileCopy;
import static org.eclipse.thym.core.internal.util.FileUtils.toURL;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.core.engine.HybridMobileLibraryResolver;
import org.eclipse.thym.core.platform.AbstractProjectGeneratorDelegate;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.wp.core.WPCore;
import org.eclipse.thym.wp.core.WPLibraryResolver;
import org.eclipse.thym.wp.internal.core.Messages;

/**
 * @author Wojciech Galanciak, 2014
 * 
 */
@SuppressWarnings("restriction")
public class WPProjectGenerator extends AbstractProjectGeneratorDelegate {

	private static final String GUID1 = "$guid1$"; //$NON-NLS-1$
	private static final String SAFE_PROJECT_NAME = "$safeprojectname$"; //$NON-NLS-1$
	private static final String PROPERTIES = "Properties"; //$NON-NLS-1$

	public WPProjectGenerator() {
		super();
	}

	public WPProjectGenerator(IProject project, File generationFolder,
			String platform) {
		init(project, generationFolder, platform);
	}

	@Override
	protected void generateNativeFiles(HybridMobileLibraryResolver resolver)
			throws CoreException {
		try {
			HybridProject hybridProject = HybridProject
					.getHybridProject(getProject());
			if (hybridProject == null) {
				throw new CoreException(new Status(IStatus.ERROR,
						WPCore.PLUGIN_ID,
						Messages.WPProjectGenerator_NotHybridError));
			}

			File destinationDir = getDestination();
			Path destinationPath = new Path(destinationDir.toString());

			String appName = hybridProject.getBuildArtifactAppName();
			Widget widgetModel = WidgetModel.getModel(hybridProject)
					.getWidgetForRead();
			String packageName = widgetModel.getId();

			if (!destinationDir.exists()) {// create the project directory
				destinationDir.mkdirs();
			}
			String safeAppName = appName.replaceAll(
					"/(\\.\\s|\\s\\.|\\s+|\\.+)/g", "_"); //$NON-NLS-1$ //$NON-NLS-2$
			packageName = packageName.replaceAll("_", "-"); //$NON-NLS-1$ //$NON-NLS-2$

			// /${project_name}
			directoryCopy(resolver.getTemplateFile(new Path(VAR_APP_NAME)),
					toURL(destinationDir));

			// copy VERSION file to project's root directory
			fileCopy(resolver.getTemplateFile(new Path(
					WPLibraryResolver.VERSION)),
					toURL(destinationPath.append(WPLibraryResolver.VERSION)
							.toFile()));

			// copy config.xml to /${project_name}/config.xml
			File configFile = getConfigFile(hybridProject.getProject());
			fileCopy(
					toURL(configFile),
					toURL(destinationPath.append(
							PlatformConstants.FILE_XML_CONFIG).toFile()));

			UUID guid = UUID.randomUUID();
			HashMap<String, String> values = new HashMap<String, String>();
			values.put(SAFE_PROJECT_NAME, appName);
			values.put(GUID1, guid.toString());

			// /${project_name}/Properties/WMAppManifest.xml
			IPath wpAppManifest = destinationPath.append(PROPERTIES).append(
					WPLibraryResolver.WP_APP_MANIFEST_XML);
			templatedFileCopy(resolver.getTemplateFile(new Path(
					WPLibraryResolver.WP_APP_MANIFEST_XML)),
					toURL(wpAppManifest.toFile()), values);

			values.clear();
			values.put(SAFE_PROJECT_NAME, packageName);

			// /${project_name}/App.xaml
			templatedFileCopy(resolver.getTemplateFile(new Path(
					WPLibraryResolver.APP_XAML)),
					toURL(destinationPath.append(WPLibraryResolver.APP_XAML)
							.toFile()), values);

			// /${project_name}/App.xaml.cs
			templatedFileCopy(resolver.getTemplateFile(new Path(
					WPLibraryResolver.APP_XAML_CS)), toURL(destinationPath
					.append(WPLibraryResolver.APP_XAML_CS).toFile()), values);

			// /${project_name}/MainPage.xaml
			templatedFileCopy(resolver.getTemplateFile(new Path(
					WPLibraryResolver.MAIN_PAGE_XAML)), toURL(destinationPath
					.append(WPLibraryResolver.MAIN_PAGE_XAML).toFile()), values);

			// /${project_name}/MainPage.xaml.cs
			templatedFileCopy(
					resolver.getTemplateFile(new Path(
							WPLibraryResolver.MAIN_PAGE_XAML_CS)),
					toURL(destinationPath.append(
							WPLibraryResolver.MAIN_PAGE_XAML_CS).toFile()),
					values);

			// /${project_name}/CordovaWP8AppProj.csproj
			templatedFileCopy(
					resolver.getTemplateFile(new Path(
							WPLibraryResolver.DEFAULT_APP_NAME_CSPROJ)),
					toURL(destinationPath.append(
							safeAppName + WPProjectUtils.CSPROJ_EXTENSION)
							.toFile()), values);

			// remove default .csproj file if exists
			File defaultCsprojFile = destinationPath.append(
					WPLibraryResolver.DEFAULT_APP_NAME_CSPROJ).toFile();
			if (defaultCsprojFile.exists()) {
				org.apache.commons.io.FileUtils
						.deleteQuietly(defaultCsprojFile);
			}

			values.put(WPLibraryResolver.DEFAULT_APP_NAME, safeAppName);

			// /${project_name}/CordovaWP8AppProj.sln
			templatedFileCopy(resolver.getTemplateFile(new Path(
					WPLibraryResolver.DEFAULT_SLN_NAME)), toURL(destinationPath
					.append(safeAppName + WPProjectUtils.SLN_EXTENSION)
					.toFile()), values);

			// remove default .sln file if exists
			File defaultSlnFile = destinationPath.append(
					WPLibraryResolver.DEFAULT_SLN_NAME).toFile();
			if (defaultSlnFile.exists()) {
				org.apache.commons.io.FileUtils.deleteQuietly(defaultSlnFile);
			}

			org.apache.commons.io.FileUtils.deleteQuietly(new File(
					destinationDir, "Bin")); //$NON-NLS-1$

			org.apache.commons.io.FileUtils.deleteQuietly(new File(
					destinationDir, "obj")); //$NON-NLS-1$

			org.apache.commons.io.FileUtils.deleteQuietly(new File(
					destinationDir, "__PreviewImage.jpg")); //$NON-NLS-1$

			org.apache.commons.io.FileUtils.deleteQuietly(new File(
					destinationDir, "__TemplateIcon.png")); //$NON-NLS-1$

			org.apache.commons.io.FileUtils.deleteQuietly(new File(
					destinationDir, "MyTemplate.vstemplate")); //$NON-NLS-1$
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, WPCore.PLUGIN_ID,
					Messages.WPProjectGenerator_GenerationError, e));
		}
	}

	@Override
	protected void replaceCordovaPlatformFiles(
			HybridMobileLibraryResolver resolver) throws IOException {
		// cordova.js in in www folder for project's template
	}

	@Override
	protected File getPlatformWWWDirectory() {
		return WPProjectUtils.getPlatformWWWDirectory(getDestination());
	}

	private File getConfigFile(IProject project) {
		IFile configFile = project.getFile(new Path(PlatformConstants.DIR_WWW)
				.append(PlatformConstants.FILE_XML_CONFIG));
		if (!configFile.exists()) {
			configFile = project.getFile(PlatformConstants.FILE_XML_CONFIG);
		}
		return configFile.getLocation().toFile();
	}

}
