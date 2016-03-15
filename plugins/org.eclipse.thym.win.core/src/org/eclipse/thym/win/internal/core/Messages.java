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

package org.eclipse.thym.win.internal.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.thym.win.internal.core.messages"; //$NON-NLS-1$
	public static String WinBuild_BuildProjectTask;
	public static String WinBuild_NoHybridError;
	public static String WinEngineRepoProvider_CannotReadError;
	public static String WinLaunchDelegate_LaunchTask;
	public static String WinLaunchDelegate_NoProjectError;
	public static String WinLaunchDelegate_NotHybridError;
	public static String WinLibraryResolver_CannotDetectError;
	public static String WinLibraryResolver_MissingEngineError;
	public static String WinLibraryResolver_NotCompatibleError;
	public static String WinLibraryResolver_NoVersionError;
	public static String WinPluginInstallationActionsFactory_NotImplementMessage;
	public static String WinProjectGenerator_GenerationError;
	public static String WinProjectGenerator_NotHybridError;
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
