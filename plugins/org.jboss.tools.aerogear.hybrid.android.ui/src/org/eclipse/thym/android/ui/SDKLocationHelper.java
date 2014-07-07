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
package org.eclipse.thym.android.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.android.core.AndroidConstants;
import org.eclipse.thym.android.core.AndroidCore;
import org.eclipse.thym.android.ui.internal.preferences.AndroidPreferences;
import org.eclipse.thym.android.ui.internal.statushandler.MissingSDKStatusHandler;
import org.eclipse.thym.core.HybridMobileStatus;
/**
 * Helper class for the Android SDK location. 
 * 
 * @author Gorkem Ercan
 *
 */
public class SDKLocationHelper {
	
	public static boolean defineSDKLocationIfNecessary(){
		if(isSDKLocationDefined())
			return true;
		MissingSDKStatusHandler handler = new MissingSDKStatusHandler();
		handler.handle(makeNoSDKLocationStatus());
		return isSDKLocationDefined();
	}
	
	public static String getSDKLocation()
	{
		if(isSDKLocationDefined())
			return  AndroidPreferences.getPrefs().getAndroidSDKLocation();
		return null;
	}

	public static boolean isSDKLocationDefined() {
		String sdkLocation = AndroidPreferences.getPrefs().getAndroidSDKLocation();
		return (sdkLocation != null && sdkLocation.length()>0);
	}
	
	public static HybridMobileStatus makeNoSDKLocationStatus(){
		return	new HybridMobileStatus(IStatus.ERROR, AndroidCore.PLUGIN_ID, AndroidConstants.STATUS_CODE_ANDROID_SDK_NOT_DEFINED, 
				"Android SDK location is not defined", null);
	}

}
