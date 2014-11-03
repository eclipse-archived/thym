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
package org.eclipse.thym.ios.core.xcode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.thym.ios.core.IOSCore;
import org.eclipse.thym.ios.core.simulator.IOSDevice;
import org.eclipse.thym.ios.core.simulator.IOSSimulator;
import org.eclipse.thym.ios.core.simulator.IOSSimulatorLaunchConstants;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.internal.util.ExternalProcessUtility;
import org.eclipse.thym.core.internal.util.TextDetectingStreamListener;
import org.eclipse.thym.core.platform.AbstractNativeBinaryBuildDelegate;

/**
 * Wrapper around the xcodebuild command line tool.
 * @author Gorkem Ercan
 *
 */
public class XCodeBuild extends AbstractNativeBinaryBuildDelegate{
	public static final String MIN_REQUIRED_VERSION = "6.0.0";
	
	private ILaunchConfiguration launchConfiguration;
	
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
	
	private class SecurityIdentityListParser implements IStreamListener{
		private StringBuffer buffer = new StringBuffer();
		private final Pattern pattern = Pattern.compile("\".*\"");

		@Override
		public void streamAppended(String text, IStreamMonitor monitor) {
			buffer.append(text);
		}
		public List<String> getIdentityList(){
			if (buffer == null || buffer.length() < 1)
				return Collections.emptyList();
			
			ArrayList<String> identityList = new ArrayList<String>();
			try {
				StringReader reader = new StringReader(buffer.toString());
				BufferedReader read = new BufferedReader(reader);
				String line = null;
				while ((line = read.readLine()) != null) {
					Matcher matcher = pattern.matcher(line);
					if(matcher.find()){
						String identity = matcher.group();
						identityList.add(identity);
					}
				}
			}catch(IOException e){
				IOSCore.log(IStatus.ERROR, "Error parsing the iOS identity list ", e);
				return Collections.emptyList();
			}
			return identityList;
		}
	}
	
	/**
	 * Returns the actual folder where the build artifacts can be found.
	 * 
	 * @param xcodeProjectFolder
	 * @return folder with the build artifacts
	 */
	public static File getBuildDir(File xcodeProjectFolder){
		return new File(xcodeProjectFolder,"build");
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
	
	public List<String> findCodesigningIdentity() throws CoreException{
		ExternalProcessUtility processUtility = new ExternalProcessUtility();
		SecurityIdentityListParser parser = new SecurityIdentityListParser();
		processUtility.execSync("security find-identity -p codesigning -v", null, parser, parser, new NullProgressMonitor(), null, null);
		return parser.getIdentityList();
	}
	
	@Override
	public void buildNow(IProgressMonitor monitor) throws CoreException {

			if(monitor.isCanceled()){
				return;
			}
		
		try {
			monitor.beginTask("Build Cordova project for iOS", 10);
			//TODO: use extension point to create the generator.
			XcodeProjectGenerator creator = new XcodeProjectGenerator(getProject(),null,"ios");
			SubProgressMonitor generateMonitor = new SubProgressMonitor(monitor, 1);
			File xcodeProjectDir  = creator.generateNow(generateMonitor);
			
			monitor.worked(4);
			if(monitor.isCanceled()){
				return; 
			}

			// xcodebuild -project $PROJECT_NAME.xcodeproj -arch i386 -target
			// $PROJECT_NAME -configuration Release -sdk $SDK clean build
			// VALID_ARCHS="i386" CONFIGURATION_BUILD_DIR="$PROJECT_PATH/build"
			HybridProject hybridProject = HybridProject.getHybridProject(this.getProject());
			if(hybridProject == null ){
				throw new CoreException(new Status(IStatus.ERROR, IOSCore.PLUGIN_ID, "Not a hybrid mobile project, can not generate files"));
			}

			String name = hybridProject.getBuildArtifactAppName();

			StringBuilder cmdString = new StringBuilder("xcodebuild -project ");
			cmdString.append("\"").append(name).append(".xcodeproj").append("\"");

//			cmdString.append(" -arch i386 armv6 armv7 -target ").append(name);
			cmdString.append(" -target ").append(name);
			cmdString.append(" -configuration Release ");
		
			cmdString.append(" -sdk ").append(selectSDK());
			cmdString.append(" clean build ");
			
			cmdString.append("VALID_ARCHS=\"i386 armv6 armv7\"");
			cmdString.append(" CONFIGURATION_BUILD_DIR=").append("\"").append(getBuildDir(xcodeProjectDir).getPath()).append("\"");
			// leave signing to package
			if(isSigned()){
				cmdString.append(" CODE_SIGN_IDENTITY=").append(getSigningProperties().get("ios.identity"));
			}else{
				cmdString.append(" CODE_SIGN_IDENTITY=\"\" CODE_SIGNING_REQUIRED=NO");
			}

			ExternalProcessUtility processUtility = new ExternalProcessUtility();
			if (monitor.isCanceled()) {
				return;
			}
			monitor.worked(1);
			TextDetectingStreamListener listener = new TextDetectingStreamListener("** BUILD SUCCEEDED **");
			processUtility.execSync(cmdString.toString(), xcodeProjectDir,
					listener, listener, monitor, null, getLaunchConfiguration());
			if(!listener.isTextDetected()){
				throw new CoreException(new Status(IStatus.ERROR, IOSCore.PLUGIN_ID, "xcodebuild has failed"));
			}
			File appFile = new File(getBuildDir(xcodeProjectDir),name+".app");
			if(isRelease() && isSigned()){
				File ipaFile = new File(getBuildDir(xcodeProjectDir),name+".ipa");
				StringBuilder packageCommand = new StringBuilder("xcrun -sdk iphoneos PackageApplication");
				packageCommand.append(" -v ").append("\"").append(appFile.toString()).append("\"");
				packageCommand.append(" -o ").append("\"").append(ipaFile.toString()).append("\"");
//				packageCommand.append(" -sign " ).append(getSigningProperties().get("ios.identity"));
				packageCommand.append(" -embed ").append("\"").append(getSigningProperties().get("ios.provisionPath")).append("\"");
				if (monitor.isCanceled()) {
					return;
				}
				monitor.worked(1);
				processUtility.execSync(packageCommand.toString(), xcodeProjectDir,
						null,null , monitor, null, getLaunchConfiguration());
				setBuildArtifact(ipaFile);
			}else{
				setBuildArtifact(appFile);
			}
			if( !getBuildArtifact().exists()){
				throw new CoreException(new Status(IStatus.ERROR, IOSCore.PLUGIN_ID, "xcodebuild has failed: build artifact does not exist"));
			}
		} finally {
			monitor.done();
		}
	}

	private Object selectSDK() {
		if(isRelease()){
			XCodeSDK releaseSDK = findLatestSDK(false);
			if(releaseSDK != null){
				return releaseSDK.getIdentifierString();
			}
			return "iphoneos8.0";
		}
		String launchConfigDeviceId=null;
		try {
			if (getLaunchConfiguration() != null) {
				launchConfigDeviceId = getLaunchConfiguration().getAttribute(
						IOSSimulatorLaunchConstants.ATTR_DEVICE_IDENTIFIER,
						(String) null);
			}
			IOSDevice device = IOSSimulator.findDevice(launchConfigDeviceId, new NullProgressMonitor());
			
			List<XCodeSDK> sdks = this.showSdks();
			for (XCodeSDK sdk : sdks) {
				if (device != null && device.getiOSName().equals(sdk.getDescription()) && sdk.isSimulator()) {
					return sdk.getIdentifierString();
				}
			}
			XCodeSDK fallbackSDK = findLatestSDK(true);
			if (fallbackSDK != null) {
				return fallbackSDK.getIdentifierString();
			}
		} catch (CoreException e) {
			//ignored
		}
		return "iphonesimulator8.0";
	}
	
	private XCodeSDK findLatestSDK(boolean isSimulator) {
		try {
			List<XCodeSDK> sdks = this.showSdks();
			if (sdks == null || sdks.isEmpty())
				return null;
			XCodeSDK latestSDK = null;
			for (XCodeSDK sdk : sdks) {
				if (sdk.isIOS() && sdk.isSimulator() == isSimulator ) {
					if(latestSDK == null ){
						latestSDK = sdk;
					}
					double sdkver = Double.parseDouble(sdk.getVersion());
					double latestVer = Double.parseDouble(latestSDK
							.getVersion());
					if (latestVer < sdkver) {
						latestSDK = sdk;
					}
				}
			}
			return latestSDK;
		} catch (CoreException e) {
			return null;
		}

	}

	public ILaunchConfiguration getLaunchConfiguration() {
		return launchConfiguration;
	}

	public void setLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
		this.launchConfiguration = launchConfiguration;
	}
	
}
