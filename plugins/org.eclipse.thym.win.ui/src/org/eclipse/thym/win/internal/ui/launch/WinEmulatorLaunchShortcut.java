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
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.thym.ui.launch.HybridProjectLaunchShortcut;
import org.eclipse.thym.win.core.build.WinConstants;
import org.eclipse.thym.win.internal.ui.Messages;

public class WinEmulatorLaunchShortcut extends HybridProjectLaunchShortcut {

	@Override
	protected boolean validateBuildToolsReady() throws CoreException {
		return true;
	}

	@Override
	protected void updateLaunchConfiguration(ILaunchConfigurationWorkingCopy wc) {
		wc.setAttribute(WinConstants.ATTR_LAUNCH_TYPE, WinConstants.ATTR_LAUNCH_TYPE_EMULATOR);
		super.updateLaunchConfiguration(wc);
	}

	@Override
	protected String getLaunchConfigurationTypeID() {
		return WinConstants.ID_LAUNCH_CONFIG_TYPE;
	}

	@Override
	protected String getLaunchConfigurationNamePrefix(IProject project) {
		return MessageFormat.format(
				Messages.WinEmulatorLaunchShortcut_DefaultName,
				project.getName());
	}

}
