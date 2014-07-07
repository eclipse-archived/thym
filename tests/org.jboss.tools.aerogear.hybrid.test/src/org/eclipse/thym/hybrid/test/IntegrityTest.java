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
package org.eclipse.thym.hybrid.test;

import static org.junit.Assert.*;

import org.eclipse.thym.android.core.AndroidCore;
import org.eclipse.thym.android.ui.AndroidUI;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.ios.core.IOSCore;
import org.eclipse.thym.ios.ui.IOSUI;
import org.eclipse.thym.ui.HybridUI;
import org.junit.Test;


public class IntegrityTest {
	
	
	@Test
	public void allPluginsExist(){
		assertNotNull( HybridCore.getDefault());
		assertNotNull( HybridUI.getDefault());
	}
	
	@Test
	public void testPluginIDIntegrity(){
		//Core
		String symbolicName = HybridCore.getContext().getBundle().getSymbolicName();
		assertEquals(symbolicName, HybridCore.PLUGIN_ID);
		symbolicName = HybridUI.getDefault().getBundle().getSymbolicName();
		assertEquals(symbolicName, HybridUI.PLUGIN_ID);
		//Android
		symbolicName  = AndroidCore.getContext().getBundle().getSymbolicName();
		assertEquals(symbolicName, AndroidCore.PLUGIN_ID);
		symbolicName = AndroidUI.getDefault().getBundle().getSymbolicName();
		assertEquals(symbolicName, AndroidUI.PLUGIN_ID);
		//IOS
		symbolicName = IOSCore.getContext().getBundle().getSymbolicName();
		assertEquals(symbolicName, IOSCore.PLUGIN_ID);
		symbolicName = IOSUI.getDefault().getBundle().getSymbolicName();
		assertEquals(symbolicName, IOSUI.PLUGIN_ID);
	}
	
	

}
