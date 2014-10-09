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

public class IOSDevice {
	
	private String deviceId;
	private String deviceName;
	private String iOSName;
	
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getiOSName() {
		return iOSName;
	}
	public void setiOSName(String iOSName) {
		this.iOSName = iOSName;
	}
	
	@Override
	public String toString() {
		return deviceName +" ("+deviceId+") ["+iOSName+"]";
	}

}
