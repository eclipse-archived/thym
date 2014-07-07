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
package org.eclipse.thym.ui.plugins.navigator.internal;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.thym.ui.plugins.internal.LaunchCordovaPluginWizardAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

public class PluginActionProvider extends CommonActionProvider {
	private LaunchCordovaPluginWizardAction launchWizardAction;
	
	
	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		launchWizardAction = new LaunchCordovaPluginWizardAction();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
	}
	
	@Override
	public void fillContextMenu(IMenuManager menu) {
		if(launchWizardAction != null ){
			menu.insertBefore(IWorkbenchActionConstants.MB_ADDITIONS, new LaunchCordovaPluginWizardAction());
		}
	}

}
