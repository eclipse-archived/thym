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

package org.eclipse.thym.win.internal.ui.launch;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.core.HybridMobileStatus;
import org.eclipse.thym.ui.launch.HybridProjectLaunchShortcut;
import org.eclipse.thym.win.core.WinCore;
import org.eclipse.thym.win.core.vstudio.WinConstants;
import org.eclipse.thym.win.internal.ui.Messages;

/**
 * Launch shortcut for Windows Phone 8 applications.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */
public class WinEmulatorLaunchShortcut extends HybridProjectLaunchShortcut {

	@Override
	protected boolean validateBuildToolsReady() throws CoreException {
		return true;
	}

	@Override
	protected String getLaunchConfigurationTypeID() {
		return WinConstants.ID_LAUNCH_CONFIG_TYPE;
	}

	@Override
	protected String getLaunchConfigurationNamePrefix(IProject project) {
		return MessageFormat.format(
				Messages.WPEmulatorLaunchShortcut_DefaultName,
				project.getName());
	}

}
