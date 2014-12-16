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
package org.eclipse.thym.wp.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.thym.wp.internal.ui.messages"; //$NON-NLS-1$
	public static String EmulatorsStatusHandler_Message;
	public static String EmulatorsStatusHandler_Title;
	public static String MSBuildStatusHandler_Message;
	public static String MSBuildStatusHandler_Title;
	public static String SDKLocationHelper_Message;
	public static String SDKStatusHandler_Message;
	public static String SDKStatusHandler_Title;
	public static String WPEmulatorLaunchShortcut_DefaultName;
	public static String WPEmulatorLaunchShortcut_MSBuildMissingMessage;
	public static String WPOptionsTab_BrowseLabel;
	public static String WPOptionsTab_DefaultEmulator;
	public static String WPOptionsTab_Description;
	public static String WPOptionsTab_DeviceName;
	public static String WPOptionsTab_EmulatorGroup;
	public static String WPOptionsTab_NoEmulatorsError;
	public static String WPOptionsTab_ProjectGroup;
	public static String WPOptionsTab_ProjectLabel;
	public static String WPOptionsTab_ProjectSelection;
	public static String WPOptionsTab_SDKNotDefinedError;
	public static String WPOptionsTab_SelectonDesc;
	public static String WPOptionsTab_TabName;
	public static String WPPreferencePage_Description;
	public static String WPPreferencePage_LocationLabel;
	public static String WPPreferencePage_NotDirectoryError;
	public static String WPPreferencePage_NotSpecifiedWarning;
	public static String WPPreferencePage_NotValidError;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
