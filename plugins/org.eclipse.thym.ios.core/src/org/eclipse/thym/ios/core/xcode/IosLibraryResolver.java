/*******************************************************************************
 * Copyright (c) 2013, 2015 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ios.core.xcode;

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
import org.eclipse.thym.ios.core.IOSCore;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.engine.HybridMobileLibraryResolver;
import org.eclipse.thym.core.internal.util.FileUtils;
import com.github.zafarkhaja.semver.Version;

/**
 * Resolves iOS cordova distros to files used by the tool.
 * @author Gorkem Ercan
 *
 */
public class IosLibraryResolver extends HybridMobileLibraryResolver {
	private static final Version VERSION_4_0_1 = Version.valueOf("4.0.1");
	private static final Version VERSION_3_3_0 = Version.valueOf("3.3.0");
	private static final Version VERSION_3_0_0 = Version.valueOf("3.0.0");
	private static final String TEMPLATEVAR_PRJ_DIR_3_0 = "__TESTING__";
	private static final String TEMPLATEVAR_PRJ_DIR_3_4 = "__PROJECT_NAME__";
	private static final String TEMPLATEVAR_PBXPROJ_3_0 = TEMPLATEVAR_PRJ_DIR_3_0;
	private static final String TEMPLATEVAR_PBXPROJ_3_4 = "__NON-CLI__";
	private static final String TEMPLATEVAR_PBXPROJ_4_1 = "__TEMP__";
	
	
	private HashMap<IPath, URL> files = new HashMap<IPath, URL>();
	
	private void initFiles() {
		IPath templatePrjRoot = libraryRoot.append("bin/templates/project");
		if(version == null ){
			return;
		}
		Version v = Version.valueOf(version);
		/* In 3.4 ios template stopped using the __TESTING__ variable 
		 * on the template. Also started using two versions for .pbxproj files
		 */
		String prjDirVar = TEMPLATEVAR_PRJ_DIR_3_0;
		String pbxProjVar = TEMPLATEVAR_PBXPROJ_3_0;
		if (v.compareWithBuildsTo(VERSION_4_0_1) > 0) {
			prjDirVar = TEMPLATEVAR_PRJ_DIR_3_4;
			pbxProjVar = TEMPLATEVAR_PBXPROJ_4_1;
		}
		else if (v.compareWithBuildsTo(VERSION_3_3_0) > 0) {
			prjDirVar = TEMPLATEVAR_PRJ_DIR_3_4;
			pbxProjVar = TEMPLATEVAR_PBXPROJ_3_4;
		}
		
		
		if(v.equals(VERSION_3_0_0)){
			files.put(new Path("cordova"), getEngineFile(libraryRoot.append("bin/templates/project/cordova/")));
		}
		else{
			files.put(new Path("cordova"), getEngineFile(libraryRoot.append("bin/templates/scripts/cordova/")));
		}
		if (v.compareWithBuildsTo(VERSION_4_0_1) > 0) {
			files.put(new Path("cordova/node_modules"), getEngineFile(libraryRoot.append("node_modules/")));
		}
		else {
			files.put(new Path("cordova/node_modules"), getEngineFile(libraryRoot.append("bin/node_modules/")));
		}
		files.put(new Path(VAR_APP_NAME), getEngineFile(templatePrjRoot.append(prjDirVar)));
		files.put(new Path(VAR_APP_NAME+"/"+VAR_APP_NAME+"-Info.plist"), getEngineFile(templatePrjRoot.append(NLS.bind("{0}/{0}-Info.plist",prjDirVar))));
		files.put(new Path(VAR_APP_NAME+"/"+VAR_APP_NAME+"-Prefix.pch") , getEngineFile(templatePrjRoot.append(NLS.bind("{0}/{0}-Prefix.pch",prjDirVar))));
		files.put(new Path(VAR_APP_NAME+".xcodeproj/project.pbxproj"), getEngineFile(templatePrjRoot.append(NLS.bind("{0}.xcodeproj/project.pbxproj",pbxProjVar))));
		files.put(new Path(VAR_APP_NAME+"/Classes/AppDelegate.h"), getEngineFile(templatePrjRoot.append(NLS.bind("{0}/Classes/AppDelegate.h", prjDirVar))));
		files.put(new Path(VAR_APP_NAME+"/Classes/AppDelegate.m"), getEngineFile(templatePrjRoot.append(NLS.bind("{0}/Classes/AppDelegate.m", prjDirVar))));
		files.put(new Path(VAR_APP_NAME+"/Classes/MainViewController.h"), getEngineFile(templatePrjRoot.append(NLS.bind("{0}/Classes/MainViewController.h", prjDirVar))));
		files.put(new Path(VAR_APP_NAME+"/Classes/MainViewController.m"), getEngineFile(templatePrjRoot.append(NLS.bind("{0}/Classes/MainViewController.m", prjDirVar))));
		files.put(new Path(VAR_APP_NAME+"/main.m"), getEngineFile(templatePrjRoot.append(NLS.bind("{0}/main.m",prjDirVar))));
		
		files.put(new Path("CordovaLib"), getEngineFile(libraryRoot.append("CordovaLib")));
		files.put(PATH_CORDOVA_JS, getEngineFile(libraryRoot.append("CordovaLib").append(PATH_CORDOVA_JS)) );
	}

	@Override
	public URL getTemplateFile(IPath destination) {
		if(files.isEmpty()) initFiles();
		Assert.isNotNull(destination);
		Assert.isTrue(!destination.isAbsolute());
		return files.get(destination);
	}
	
	@Override
	public IStatus isLibraryConsistent() {
		if(version == null ){
			return new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Library for iOS platform is not compatible with this tool. VERSION file is missing.");
		}
		if(files.isEmpty()) initFiles();
		Iterator<IPath> paths = files.keySet().iterator();
		while (paths.hasNext()) {
			IPath key = paths.next();
			URL url = files.get(key);
			if(url != null  ){
				File file = new File(url.getFile());
				if( file.exists()){
					continue;
				}
			}
			return new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, NLS.bind("Library for iOS platform is not compatible with this tool. File for path {0} is missing.",key.toString()));
		}
		return Status.OK_STATUS;
	}
 
	private URL getEngineFile(IPath path){
		File file = path.toFile();
		if(!file.exists()){
			HybridCore.log(IStatus.ERROR, "missing iOS engine file " + file.toString(), null );
		}
		return FileUtils.toURL(file);
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
		File versionFile = this.libraryRoot.append("CordovaLib").append("VERSION").toFile();
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
				IOSCore.log(IStatus.ERROR, "Can not detect version on library",
						e);
			}
		}else{
			IOSCore.log(IStatus.ERROR, NLS.bind("Can not detect version. VERSION file {0} is missing",versionFile.toString()), null);
		}
		return null;
	}
}
