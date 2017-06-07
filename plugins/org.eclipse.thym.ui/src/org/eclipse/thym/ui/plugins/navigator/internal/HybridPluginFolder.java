/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.plugins.navigator.internal;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.thym.core.plugin.CordovaPlugin;

public class HybridPluginFolder extends PlatformObject{
	
	private IFolder folder;
	private CordovaPlugin plugin;
	
	public HybridPluginFolder(IFolder folder, CordovaPlugin plugin) {
		this.folder = folder;
		this.plugin = plugin;
	}

	public IFolder getFolder() {
		return folder;
	}

	public CordovaPlugin getPlugin() {
		return plugin;
	}
	
	

}
