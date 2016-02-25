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
package org.eclipse.thym.win.internal.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.thym.win.internal.core.messages"; //$NON-NLS-1$
	public static String MSBuild_BuildProjectTask;
	public static String MSBuild_MSBuildError;
	public static String MSBuild_MSBuildFailedMessage;
	public static String MSBuild_NoHybridError;
	public static String MSBuild_NoMSBuildError;
	public static String WPEmulator_NoDeployCmdError;
	public static String WPEngineRepoProvider_CannotReadError;
	public static String WPLaunchDelegate_LaunchTask;
	public static String WPLaunchDelegate_NoEmulatorsError;
	public static String WPLaunchDelegate_NoProjectError;
	public static String WPLaunchDelegate_NotHybridError;
	public static String WPLaunchDelegate_SDKMissingMessage;
	public static String WPLibraryResolver_CannotDetectError;
	public static String WPLibraryResolver_MissingEngineError;
	public static String WPLibraryResolver_NotCompatibleError;
	public static String WPLibraryResolver_NoVersionError;
	public static String WPPluginInstallationActionsFactory_NotImplementMessage;
	public static String WPProjectGenerator_GenerationError;
	public static String WPProjectGenerator_NotHybridError;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
