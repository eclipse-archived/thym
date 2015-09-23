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
package org.eclipse.thym.ios.core.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.thym.core.internal.util.ExternalProcessUtility;
import org.eclipse.thym.ios.core.IOSCore;
/**
 * Wrapper around the native binary for controlling the iOS Simulator.
 * 
 * @author Gorkem Ercan
 *
 */
public class IOSSimulator {
	

	private String[] environment;
	private IOSDevice deviceId;
	private IProgressMonitor monitor;

	private static class DeviceListParser implements IStreamListener{
		private StringBuffer buffer = new StringBuffer();

		@Override
		public void streamAppended(String text, IStreamMonitor monitor) {
			buffer.append(text);
		}
		
		public List<IOSDevice> getDeviceList(){
			if (buffer == null || buffer.length() < 1)
				return null;
			
			try {
				StringReader reader = new StringReader(buffer.toString());
				BufferedReader read = new BufferedReader(reader);
				String line = null;
				String iosVersion = null;
				boolean parsingDevices = false;
				List<IOSDevice> devices = new ArrayList<IOSDevice>();
				while ((line = read.readLine()) != null) {
					if (line.isEmpty())
						continue;
					if (line.equals("== Devices ==")) {
						parsingDevices = true;
						continue;
					}
					if (parsingDevices) {
						if (line.startsWith("==") ){
							break;
						}else
						if (line.startsWith("--")) {
							line = line.replace("--", "");
							iosVersion = line.trim();
						} else {
							String[] parts = line.split("[\\(\\)]");
							if ( parts.length<2 ){
								continue;
							}
							IOSDevice device = new IOSDevice();
							device.setDeviceName(parts[0].trim());
							device.setDeviceId(parts[1].trim());
							device.setiOSName(iosVersion);
							devices.add(device);
						}
					}
				}
				return devices;
			}catch(IOException e){
				IOSCore.log(IStatus.ERROR, "error parsing device list", e);
				return Collections.emptyList();
			}
		}
	}
	
	public IOSSimulator(IOSDevice deviceId){
		this.setDeviceId(deviceId);
	}
	
	public IOSSimulator launch() throws CoreException{
		StringBuilder cmdLine = new StringBuilder();
		cmdLine.append("xcrun");
		cmdLine.append(" instruments -w ");
		cmdLine.append(getDeviceId().getDeviceId());
		ExternalProcessUtility processUtility = new ExternalProcessUtility();
		processUtility.execSync(cmdLine.toString(),null, null, null, getProgressMonitor(),environment,null);
		return this;
	}
	
	public IOSSimulator installApp( String path ) throws CoreException{
		StringBuilder cmdLine = new StringBuilder();
		cmdLine.append("xcrun");
		cmdLine.append(" simctl install ");
		cmdLine.append(getDeviceId().getDeviceId()).append(" ");
		cmdLine.append("\"").append( path ).append("\"");
		ExternalProcessUtility processUtility = new ExternalProcessUtility();
		processUtility.execSync(cmdLine.toString(),null, null, null, getProgressMonitor(),environment,null);
		return this;
	}

	public IOSSimulator startApp(String id) throws CoreException{
		StringBuilder cmdLine = new StringBuilder();
		cmdLine.append("xcrun");
		cmdLine.append(" simctl launch ");
		cmdLine.append(getDeviceId().getDeviceId()).append(" ");
		cmdLine.append( id );
		ExternalProcessUtility processUtility = new ExternalProcessUtility();
		processUtility.execSync(cmdLine.toString(),null, null, null, getProgressMonitor(),environment,null);
		return this;
	}
	
	public static List<IOSDevice> listDevices(IProgressMonitor monitor) throws CoreException{
		StringBuilder cmdLine = new StringBuilder();
		cmdLine.append("xcrun");
		cmdLine.append(" simctl list ");
		ExternalProcessUtility processUtility = new ExternalProcessUtility();
		DeviceListParser parser = new DeviceListParser();
		processUtility.execSync(cmdLine.toString(),null, parser, parser, monitor,null ,null);
		return parser.getDeviceList();
	}
	
	public static IOSDevice findDevice(String deviceId, IProgressMonitor monitor) throws CoreException{
		if(deviceId == null ) return null;
		List<IOSDevice> devices = listDevices(monitor);
		if(devices == null ) return null;
		for (IOSDevice iosDevice : devices) {
			if(iosDevice.getDeviceId().equals(deviceId)){
				return iosDevice;
			}
		}
		return null;
	}

	/**
	 * The environment variables set in the process
	 * @param envp
	 */
	public IOSSimulator setProcessEnvironmentVariables(String[] envp) {
		this.environment = envp;
		return this;
	}

	public IOSDevice getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(IOSDevice deviceId) {
		this.deviceId = deviceId;
	}
	
	private IProgressMonitor getProgressMonitor(){
		if(monitor == null){
			monitor = new NullProgressMonitor();
		}
		return monitor;
	}
	
	public IOSSimulator setProgressMonitor(IProgressMonitor progressMonitor){
		this.monitor = progressMonitor;
		return this;
	}

}
