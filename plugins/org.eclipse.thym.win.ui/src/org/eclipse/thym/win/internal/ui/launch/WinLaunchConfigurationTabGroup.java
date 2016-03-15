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

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class WinLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		WinOptionsTab winTab = new WinOptionsTab();
		EnvironmentTab env = new EnvironmentTab();
		CommonTab common = new CommonTab();
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] { winTab,
				env, common };
		setTabs(tabs);
	}

}
