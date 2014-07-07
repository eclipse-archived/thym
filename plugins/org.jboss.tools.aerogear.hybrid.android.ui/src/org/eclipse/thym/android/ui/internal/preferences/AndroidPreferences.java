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
package org.eclipse.thym.android.ui.internal.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.thym.android.core.AndroidConstants;
/**
 * Central location for Android preferences
 * 
 * @author Gorkem Ercan
 *
 */
public class AndroidPreferences {
	
	private static AndroidPreferences thePreferences = new AndroidPreferences();
	private IPreferenceStore kernelStore;
	private volatile String androdSDKLocation;
	
	public static void init(IPreferenceStore store){
		thePreferences.kernelStore = store;
	}
	
	public static AndroidPreferences getPrefs(){
		return thePreferences;
	}
	
	public void loadValues(){
		loadValues(null);
	}
	
	public void loadValues(PropertyChangeEvent event) {
		if(event == null || event.getProperty().equals(AndroidConstants.PREF_ANDROID_SDK_LOCATION)){
			androdSDKLocation = kernelStore.getString(AndroidConstants.PREF_ANDROID_SDK_LOCATION);			
		}
	}
	
	public String getAndroidSDKLocation(){
		return androdSDKLocation;
	}
	
}
