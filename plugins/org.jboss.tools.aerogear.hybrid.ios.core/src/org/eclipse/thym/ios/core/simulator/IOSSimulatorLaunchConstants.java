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

import org.eclipse.thym.ios.core.IOSCore;
/**
 * Constants used by the iOS Simulator launch configuration type
 * @author Gorkem Ercan
 *
 */
public interface IOSSimulatorLaunchConstants {

	/**
	 * Type id for the IOSSimulator Launch 
	 */
	public static final String ID_LAUNCH_CONFIG_TYPE = "org.eclipse.thym.ios.core.IOSSimulatorLaunchConfigType";

	
	/**
	 * Device family attribute passed to ios-sim to select iphone, ipad etc..
	 */
	public static final String ATTR_DEVICE_FAMILY = IOSCore.PLUGIN_ID + ".ATTR_DEVICE_FAMILY";
	
	/**
	 * Attribute to enable retina display or not for the simulator
	 */
	public static final String ATTR_USE_RETINA = IOSCore.PLUGIN_ID + ".ATTR_USE_RETINA";

	/**
	 * Attribute to enable 64 bit device or not for the simulator
	 */
	public static final String ATTR_USE_64BIT = IOSCore.PLUGIN_ID + ".ATTR_USE_64BIT";
	
	/**
	 * Attribute to use taller device skin
	 */
	public static final String ATTR_USE_TALL = IOSCore.PLUGIN_ID + ".ATTR_USE_TALL";

	/**
	 * Attribute to pass the desired simulator version to be launched
	 */
	public static final String ATTR_SIMULATOR_SDK_VERSION = IOSCore.PLUGIN_ID + ".ATTR_SIMULATOR_SDK_VERSION";
	
	/**
	 * Value for device family attribute
	 */
	public static final String VAL_DEVICE_FAMILY_IPHONE = "iphone";
	
	/**
	 * Value for device family attribute
	 */	
	public static final String VAL_DEVICE_FAMILY_IPAD = "ipad";
	
}
