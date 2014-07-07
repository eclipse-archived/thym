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
package org.eclipse.thym.ui.internal.preferences;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
/**
 * Central location for all the preferences
 * 
 * @author Gorkem Ercan
 *
 */
public class HybridToolsPreferences {
	
	private static HybridToolsPreferences thePreferences = new HybridToolsPreferences();
	private IPreferenceStore kernelStore;
	
	public static void init(IPreferenceStore store){
		thePreferences.kernelStore = store;
	}
	
	public static HybridToolsPreferences getPrefs(){
		return thePreferences;
	}
	
	public void loadValues(){
		loadValues(null);
	}
	
	public void loadValues(PropertyChangeEvent event) {
		Assert.isNotNull(kernelStore);
	}
}
