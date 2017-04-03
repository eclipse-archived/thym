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
import org.eclipse.thym.android.ui.internal.statushandler.MissingSDKStatusHandler;
import org.eclipse.thym.core.HybridMobileStatus;
import org.eclipse.thym.ui.requirement.PlatformRequirementsHandler;
/**
 * Helper class for the Android SDK location. 
 * 
 * @author Gorkem Ercan
 *
 */
public class SDKLocationHelper implements PlatformRequirementsHandler{
	
	public static boolean defineSDKLocationIfNecessary(){
		if(AndroidCore.getSDKLocation() != null){
			return true;
		}
		MissingSDKStatusHandler handler = new MissingSDKStatusHandler();
		handler.handle(makeNoSDKLocationStatus());
		return AndroidCore.getSDKLocation() != null;
	}
	
	public static HybridMobileStatus makeNoSDKLocationStatus(){
		return	new HybridMobileStatus(IStatus.ERROR, AndroidCore.PLUGIN_ID, AndroidConstants.STATUS_CODE_ANDROID_SDK_NOT_DEFINED, 
				"Android SDK location is not defined", null);
	}

	@Override
	public void checkPlatformRequirements() {
		defineSDKLocationIfNecessary();
	}

}
