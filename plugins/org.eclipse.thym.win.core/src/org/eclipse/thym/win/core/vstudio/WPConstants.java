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
package org.eclipse.thym.win.core.vstudio;

import org.eclipse.thym.win.core.WPCore;


/**
 * @author Wojciech Galanciak, 2014
 * 
 */
public interface WPConstants {

	/**
	 * Id for the Windows Phone 8 launch configuration type.
	 */
	public static final String ID_LAUNCH_CONFIG_TYPE = "org.eclipse.thym.win.core.WPLaunchConfigurationType"; //$NON-NLS-1$

	public static final String MS_BUILD = "MSBuild.exe"; //$NON-NLS-1$

	/**
	 * Id for Windows Phone 8 SDK location preference.
	 */
	public static final String WINDOWS_PHONE_SDK_LOCATION_PREF = "windowsPhoneSDKLocation"; //$NON-NLS-1$
	
	public static final String ATTR_DEVICE_IDENTIFIER = WPCore.PLUGIN_ID + ".ATTR_DEVICE_IDENTIFIER"; //$NON-NLS-1$
	
	public static final String SDK_DOWNLOAD_URL = "http://dev.windows.com/en-us/develop/download-phone-sdk"; //$NON-NLS-1$
	
	/**
	 * Status code for missing Windows Phone SDK.
	 */
	public static final int MISSING_SDK_STATUS_CODE = 310;
	
	/**
	 * Status code for missing MSBuild.
	 */
	public static final int MISSING_MSBUILD_STATUS_CODE = 320;
	
	/**
	 * Status code for missing Windows Phone emulators.
	 */
	public static final int MISSING_EMULATORS_STATUS_CODE = 330;

}
