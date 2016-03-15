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

package org.eclipse.thym.win.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.thym.win.internal.ui.messages"; //$NON-NLS-1$
	public static String EmulatorsStatusHandler_Message;
	public static String EmulatorsStatusHandler_Title;
	public static String MSBuildStatusHandler_Message;
	public static String MSBuildStatusHandler_Title;
	public static String WinEmulatorLaunchShortcut_DefaultName;
	public static String WinOptionsTab_BrowseLabel;
	public static String WinOptionsTab_DefaultEmulator;
	public static String WinOptionsTab_DeviceName;
	public static String WinOptionsTab_EmulatorGroup;
	public static String WinOptionsTab_ProjectGroup;
	public static String WinOptionsTab_ProjectLabel;
	public static String WinOptionsTab_ProjectSelection;
	public static String WinOptionsTab_SelectonDesc;
	public static String WinOptionsTab_TabName;
	public static String WinOptionsTab_Description;
	public static String WinPreferencePage_Description;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
