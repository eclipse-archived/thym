/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Red Hat Inc. - initial API and implementation and/or initial documentation
 *		Zend Technologies Ltd. - initial implementation
 *		IBM Corporation - initial API and implementation
 *******************************************************************************/  

package org.eclipse.thym.win.core.build;

import org.eclipse.thym.win.core.WinCore;

public interface WinConstants {


  // Windows Universal launch configuration type ID
  public static final String ID_LAUNCH_CONFIG_TYPE = "org.eclipse.thym.win.core.WinLaunchConfigurationType"; //$NON-NLS-1$

  /**
   * Id for Windows Phone 8 SDK location preference.
   */

  // Windows Universal 
  public static final String WINDOWS_PHONE_SDK_LOCATION_PREF = "windowsPhoneSDKLocation"; //$NON-NLS-1$

  public static final String ATTR_DEVICE_IDENTIFIER = WinCore.PLUGIN_ID + ".ATTR_DEVICE_IDENTIFIER"; //$NON-NLS-1$

  public static final String SDK_DOWNLOAD_URL = "http://dev.windows.com/en-us/develop/download-phone-sdk"; //$NON-NLS-1$


  // Windows Universal Launch Types
  public static final String ATTR_LAUNCH_TYPE = WinCore.PLUGIN_ID + ".ATTR_LAUNCH_TYPE_EMULATOR";
  public static final String ATTR_LAUNCH_TYPE_EMULATOR = "ATTR_LAUNCH_TYPE_EMULATOR";
  public static final String ATTR_LAUNCH_TYPE_SIMULATOR = "ATTR_LAUNCH_TYPE_SIMULATOR";
  public static final String ATTR_LAUNCH_TYPE_DEVICE = "ATTR_LAUNCH_TYPE_DEVICE";



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
