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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.internal.cordova.CordovaCLI;
import org.eclipse.thym.core.internal.util.ExternalProcessUtility;
import org.eclipse.thym.core.platform.AbstractNativeBinaryBuildDelegate;
import org.eclipse.thym.ios.core.IOSCore;

/**
 * Wrapper around ios build for Cordova CLI
 * 
 * @author Gorkem Ercan
 *
 */
public class XCodeBuild extends AbstractNativeBinaryBuildDelegate{
	public static final String MIN_REQUIRED_VERSION = "6.0.0";
	
	private class SDKListParser implements IStreamListener{
		private StringBuffer buffer = new StringBuffer();
		@Override
		public void streamAppended(String text, IStreamMonitor monitor) {
			buffer.append(text);
			
		}
		
		ArrayList<XCodeSDK> getSDKList(){
			ArrayList<XCodeSDK> sdkList=new ArrayList<XCodeSDK>(5);
			if(buffer.indexOf("-sdk")>0){
				String text = buffer.toString();
				text = text.replaceAll("[ a-zA-Z1-9.1-9]*:", "");
				text = text.replaceAll("-sdk [a-z]*[0-9]*.[0-9]*", "");
				String[] sdks = text.split("\n");
				for (String string : sdks) {
					String clean = string.trim();
					if(!clean.isEmpty()){
					sdkList.add(new XCodeSDK(clean));
					}
				}
			}
			return sdkList;
		}
	}

	
	private class XCodeVersionParser implements IStreamListener{
		private StringBuffer buffer = new StringBuffer();
		
		@Override
		public void streamAppended(String text, IStreamMonitor monitor) {
			buffer.append(text);
		}
		
		public String getVersion(){
			return buffer.substring("XCode".length()+1, buffer.indexOf("\n"));
		}
		
		
	}

	
	private ILaunchConfiguration launchConfiguration;
	
	@Override
	public void buildNow(IProgressMonitor monitor) throws CoreException {

		if (monitor.isCanceled()) {
			return;
		}
		SubMonitor sm = SubMonitor.convert(monitor, "Build project for iOS", 100);

		try {

			HybridProject hybridProject = HybridProject.getHybridProject(this.getProject());
			if (hybridProject == null) {
				throw new CoreException(new Status(IStatus.ERROR, IOSCore.PLUGIN_ID,
						"Not a hybrid mobile project, can not generate files"));
			}
			String buildType = "--emulator";
			if(isRelease()){
				buildType = "--device";
			}
			CordovaCLI.newCLIforProject(hybridProject).build(sm.newChild(90), "ios",buildType);
			if (sm.isCanceled()) {
				return;
			}
			String name = hybridProject.getAppName();
			IFolder buildFolder = hybridProject.getProject().getFolder("platforms/ios/build");
			IFolder artifactFolder = null;
			if(isRelease()){
				artifactFolder = buildFolder.getFolder("device");
			}else{
				artifactFolder = buildFolder.getFolder("emulator");
			}
			
			setBuildArtifact(new File(artifactFolder.getLocation().toFile(), name + ".app"));
			if (!getBuildArtifact().exists()) {
				throw new CoreException(new Status(IStatus.ERROR, IOSCore.PLUGIN_ID,
						"xcodebuild has failed: build artifact does not exist"));
			}
		} finally {
			sm.done();
		}
	}
	
	public ILaunchConfiguration getLaunchConfiguration() {
		return launchConfiguration;
	}

	public void setLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
		this.launchConfiguration = launchConfiguration;
	}
	
	public List<XCodeSDK> showSdks() throws CoreException {
		ExternalProcessUtility processUtility = new ExternalProcessUtility();
		SDKListParser parser = new SDKListParser();
		processUtility.execSync("xcodebuild -showsdks ", 
				null, parser, parser, new NullProgressMonitor(), null, null);
		return parser.getSDKList();
	}
	
	public String version() throws CoreException{
		ExternalProcessUtility processUtility = new ExternalProcessUtility();
		XCodeVersionParser parser = new XCodeVersionParser();
		processUtility.execSync("xcodebuild -version", 
				null, parser, parser, new NullProgressMonitor(), null, null);
		return parser.getVersion();
	}
	
}
