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
import org.eclipse.thym.ui.plugins.internal.PluginUninstallAction;
import org.eclipse.ui.navigator.CommonActionProvider;

public class CordovaPluginActionProvider extends CommonActionProvider {
	
	@Override
	public void fillContextMenu(IMenuManager menu) {
		menu.add(new PluginUninstallAction());
	}

}
