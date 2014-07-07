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
/**
 * Object that represents an (either emulator or a real android device 
 * 
 * @author Gorkem Ercan
 *
 */
public class AndroidDevice {
	
	/**
	 * No device is connected
	 */
	public static final int STATE_NODEVICE = 0;
	
	/**
	 * Device is connected but not responding
	 */
	public static final int STATE_OFFLINE = 1;
	
	/**
	 * Device is connected
	 */
	public static final int STATE_DEVICE = 2;
	
	
	private String serialNumber;
	private int state;
	private boolean emulator;
	
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public boolean isEmulator() {
		return emulator;
	}
	public void setEmulator(boolean emulator) {
		this.emulator = emulator;
	}

}
