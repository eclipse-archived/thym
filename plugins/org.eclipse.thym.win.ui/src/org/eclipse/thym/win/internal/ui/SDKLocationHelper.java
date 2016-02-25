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
package org.eclipse.thym.win.internal.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.core.HybridMobileStatus;
import org.eclipse.thym.win.core.WPCore;
import org.eclipse.thym.win.core.vstudio.WPConstants;
import org.eclipse.thym.win.internal.ui.statushandler.SDKStatusHandler;

/**
 * Helper class for Windows Phone 8 SDK location.
 * 
 * @author Wojciech Galanciak, 2014
 *
 */
public class SDKLocationHelper {

	/**
	 * Check if Windows Phone 8 SDK location is defined. If not then ask user
	 * for defining it now.
	 * 
	 * @return <code>true</code> if Windows Phone 8 SDK location is defined;
	 *         otherwise return <code>false</code>
	 * @throws CoreException
	 */
	public static boolean defineSDKLocationIfNecessary() throws CoreException {
		if (isSDKLocationDefined()) {
			return true;
		}
		SDKStatusHandler handler = new SDKStatusHandler();
		handler.handle(makeMissingSDKLocationStatus());
		return isSDKLocationDefined();
	}

	/**
	 * @return path to Windows Phone SDK location or <code>null</code> if not
	 *         defined
	 * @throws CoreException
	 */
	public static String getSDKLocation() throws CoreException {
		return isSDKLocationDefined() ? WPCore.getSDKLocation() : null;
	}

	/**
	 * @return <code>true</code> if Windows Phone 8 SDK location is defined in
	 *         preferences; otherwise return <code>false</code>
	 * @throws CoreException
	 */
	public static boolean isSDKLocationDefined() throws CoreException {
		String sdkLocation = WPCore.getSDKLocation();
		return sdkLocation != null && sdkLocation.length() > 0;
	}

	/**
	 * Create status for missing SDK location.
	 * 
	 * @return {@link HybridMobileStatus} instance
	 */
	public static HybridMobileStatus makeMissingSDKLocationStatus() {
		return new HybridMobileStatus(IStatus.ERROR, WPCore.PLUGIN_ID,
				WPConstants.MISSING_SDK_STATUS_CODE,
				Messages.SDKLocationHelper_Message, null);
	}

}
