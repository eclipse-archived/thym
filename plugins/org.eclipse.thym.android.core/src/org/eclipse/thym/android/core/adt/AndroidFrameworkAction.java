/*******************************************************************************
 * Copyright (c) 2013, 2014 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.android.core.adt;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.thym.android.core.AndroidCore;
import org.eclipse.thym.core.platform.IPluginInstallationAction;

public class AndroidFrameworkAction implements IPluginInstallationAction {
	
	private final IPath source;
	private final File projectDir;
	private final File pluginDir;
	private final boolean isCustom;
	private final String parent;
	private final String pluginId;
	private final AndroidSDK sdk;
	
	
	public AndroidFrameworkAction(String src, String custom, String parent, String pluginId, File projectDir, File pluginDir, AndroidSDK sdk ) {
		source = new Path(src);
		this.parent = parent;
		this.projectDir = projectDir;
		this.pluginDir = pluginDir;
		this.pluginId = pluginId;
		this.sdk = sdk;
		if(custom == null ){
			isCustom = false;
		}else{
			isCustom = Boolean.parseBoolean(custom);
		}
		
	}

	@Override
	public String[] filesToOverwrite() {
		return null;
	}

	@Override
	public void install() throws CoreException {
		
		String libref ="";
		if(isCustom){
			IPath dest = new Path(this.projectDir.toString()).append(getFrameworkSubDir());
			try {
				File destFile = new File(dest.toString());
				FileUtils.copyDirectory(new File(pluginDir,source.toString()), destFile);
				AndroidSDKManager.getManager().updateProject(sdk, null, true, destFile, new NullProgressMonitor());
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, AndroidCore.PLUGIN_ID, "Copying custom framework failed",e));
			}
			libref = getFrameworkSubDir().toString();
		}else{
			File sdkDir = new File(AndroidCore.getSDKLocation());
			File subSDK = new File(sdkDir, source.toString() );
			libref = subSDK.toString();
		}
		
		try {
			File propertiesFile = getPropertiesFile();
			Properties properties = new Properties();
			properties.load(new FileReader(propertiesFile));

			int index = 1;
			String keyName = "android.library.reference."
					+ Integer.toString(index);
			while (properties.containsKey(keyName)) {
				++index;
				keyName = "android.library.reference."
						+ Integer.toString(index);
			}
			properties.put(keyName, libref);
			properties.store(new FileWriter(propertiesFile),
					"Updated by Eclipse THyM");
			
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, AndroidCore.PLUGIN_ID, "Updating the project.properties file has failed",e));
		}		
	}

	@Override
	public void unInstall() throws CoreException {
		String libref ="";
		if(isCustom){
			IPath dest = new Path(this.projectDir.toString()).append(getFrameworkSubDir());
			File destFile = new File(dest.toString());
			FileUtils.deleteQuietly(destFile);
			libref = getFrameworkSubDir().toString();
		}else{
			File sdkDir = new File(AndroidCore.getSDKLocation());
			File subSDK = new File(sdkDir, source.toString() );
			libref = subSDK.toString();
		}
		try {
			File propertiesFile = getPropertiesFile();
			Properties properties = new Properties();
			properties.load(new FileReader(propertiesFile));

			int index = 1;
			String keyName = "android.library.reference."
					+ Integer.toString(index);
			while (properties.containsKey(keyName)) {
				String val = properties.getProperty(keyName);
				if(val.equals(libref)){
					properties.remove(keyName);
					break;
				}
				++index;
				keyName = "android.library.reference."
						+ Integer.toString(index);
			}
			properties.store(new FileWriter(propertiesFile),
					"Updated by Eclipse THyM");
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, AndroidCore.PLUGIN_ID, "Updating the project.properties file has failed",e));
		}	
	}

	private File getPropertiesFile() {
		File propertiesFile;
		if(parent == null || parent.equals(".")){
			propertiesFile =  new File(this.projectDir,"project.properties");
		}else{
	 		propertiesFile = new File(this.projectDir, pluginId+ File.separator + parent +File.separator +"project.properties");
		}
		return propertiesFile;
	}

	private IPath getFrameworkSubDir(){
		IPath subdir = new Path(pluginId);
		if(parent != null ){
			subdir = subdir.append(parent);
		}
		return subdir.append(source.lastSegment());
	}

}
