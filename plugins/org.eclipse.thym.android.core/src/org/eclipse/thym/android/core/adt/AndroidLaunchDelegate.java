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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.android.core.AndroidConstants;
import org.eclipse.thym.android.core.AndroidCore;
import org.eclipse.thym.core.HybridMobileStatus;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.HybridProjectLaunchConfigConstants;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;

public class AndroidLaunchDelegate implements ILaunchConfigurationDelegate2 {

	private File artifact;
	private AndroidDevice device;

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if(device == null ){
			throw new CoreException(new Status(IStatus.ERROR, AndroidCore.PLUGIN_ID,
					"Failed to connect with the device or emulator. We will attempt to reconnect, please try running your application again."));
		}
		AndroidSDKManager sdk = AndroidSDKManager.getManager();

		HybridProject project = HybridProject.getHybridProject(getProject(configuration));
		WidgetModel model = WidgetModel.getModel(project);
		Widget widget = model.getWidgetForRead();
		String packageName = widget.getId();
		
		String name = "MainActivity";

		sdk.installApk(artifact, device.getSerialNumber(),monitor);

		sdk.startApp(packageName+"/."+name, device.getSerialNumber(),monitor);
		String logcatFilter = configuration.getAttribute(AndroidLaunchConstants.ATTR_LOGCAT_FILTER, AndroidLaunchConstants.VAL_DEFAULT_LOGCAT_FILTER);
		sdk.logcat(logcatFilter,null,null, device.getSerialNumber());

	}

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
			throws CoreException {
		return null;
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		if(monitor.isCanceled() ){
			return false;
		}
		BuildDelegate buildDelegate = new BuildDelegate();
		buildDelegate.init(getProject(configuration), null);
		buildDelegate.buildNow(monitor);
		artifact = buildDelegate.getBuildArtifact();
		return true;
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		monitor.done();
		return true;
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		// Start ADB Server
		boolean runOnDevice = configuration.getAttribute(AndroidLaunchConstants.ATTR_IS_DEVICE_LAUNCH, false);
		AndroidSDKManager sdk = AndroidSDKManager.getManager();
		if(!runOnDevice){
			sdk.killADBServer();
		}
		sdk.startADBServer();

		if(runOnDevice){
			String  serial = configuration.getAttribute(AndroidLaunchConstants.ATTR_DEVICE_SERIAL, (String)null);
			Assert.isNotNull(serial);
			List<AndroidDevice> devices = sdk.listDevices();
			for (AndroidDevice androidDevice : devices) {
				if( !androidDevice.isEmulator()){ // We want a device (not emulator)
					this.device = androidDevice;
				}
				//Prefer the device with given serial if available.
				//This is probably important if there are multiple devices that are
				//connected.
				if(serial.equals(androidDevice.getSerialNumber()))
				{
					this.device = androidDevice;
					break;
				}
			}
			if(this.device != null )
			{
				monitor.done();
				return true;
			}else{
				throw new CoreException(new Status(IStatus.ERROR, AndroidCore.PLUGIN_ID, "Could not establish connection with the device. Please try again."));
			}
		}

		//Run emulator
		AndroidDevice emulator = getEmulator();
		// Do we have any emulators to run on?
		if ( emulator == null ){
			// No emulators lets start an emulator.
			// Check if we have an AVD
			String avdName = selectAVD(configuration, sdk);
			if(monitor.isCanceled()){
				return false;
			}
			//start the emulator.
			IProcess emulatorProcess = sdk.startEmulator(avdName);
			// wait for it to come online
			sdk.waitForEmulator(emulatorProcess, monitor);
		}
		this.device = getEmulator();
		if(this.device == null ){// This is non-sense so is adb
			sdk.killADBServer();
			sdk.startADBServer();
			this.device = getEmulator();
		}
		monitor.done();
		return true;
	}

	private String selectAVD(ILaunchConfiguration configuration, AndroidSDKManager sdk) throws CoreException{
		List<AndroidAVD> avds = sdk.listAVDs();
		if (avds == null || avds.isEmpty()){
			throw new CoreException(new HybridMobileStatus(IStatus.ERROR, AndroidCore.PLUGIN_ID, AndroidConstants.STATUS_CODE_ANDROID_AVD_ISSUE,
					"No Android AVDs are available",null));
		}
		String avdName = configuration.getAttribute(AndroidLaunchConstants.ATTR_AVD_NAME, (String)null);
		AndroidAPILevelComparator alc = new AndroidAPILevelComparator();
		for (AndroidAVD androidAVD : avds) {
			if(avdName == null ){
				if( alc.compare(androidAVD.getApiLevel(),AndroidConstants.REQUIRED_MIN_API_LEVEL) >-1){
					avdName = androidAVD.getName();
					break;
				}
			}
			else if(androidAVD.getName().equals(avdName)){
					if(alc.compare(androidAVD.getApiLevel(),AndroidConstants.REQUIRED_MIN_API_LEVEL) <0){
						throw new CoreException(new HybridMobileStatus(IStatus.ERROR, AndroidCore.PLUGIN_ID, AndroidConstants.STATUS_CODE_ANDROID_AVD_ISSUE,
								NLS.bind("Selected Android AVD {0} does not satisfy the satisfy the minimum API level({1})",
									new String[]{avdName, AndroidConstants.REQUIRED_MIN_API_LEVEL}),null));

					}
				}

			}
		if(avdName == null ){
			throw new CoreException(new HybridMobileStatus(IStatus.ERROR, AndroidCore.PLUGIN_ID, AndroidConstants.STATUS_CODE_ANDROID_AVD_ISSUE,
					NLS.bind("Defined Android AVDs do not satisfy the minimum API level({0})",AndroidConstants.REQUIRED_MIN_API_LEVEL),null));
		}
		return avdName;
	}

	private AndroidDevice getEmulator() throws CoreException{
		AndroidSDKManager sdk = AndroidSDKManager.getManager();
		List<AndroidDevice> devices = sdk.listDevices();
		for (AndroidDevice androidDevice : devices) {
			if ( androidDevice.isEmulator() && androidDevice.getState() == AndroidDevice.STATE_DEVICE )
				return androidDevice;
		}
		return null;
	}

	//TODO: duplicated form IOSLaunchDelegate... move both to a common utility.
	private IProject getProject(ILaunchConfiguration configuration){
		try{
			String projectName = configuration.getAttribute(HybridProjectLaunchConfigConstants.ATTR_BUILD_SCOPE, (String)null);
			if(projectName != null ){
				 return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			}
		}catch(CoreException e){
			return null;
		}
		return null;
	}
}
