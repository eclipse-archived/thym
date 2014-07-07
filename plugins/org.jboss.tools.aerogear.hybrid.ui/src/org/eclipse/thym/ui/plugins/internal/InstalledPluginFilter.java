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
package org.eclipse.thym.ui.plugins.internal;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.plugin.CordovaPluginManager;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPluginInfo;

public class InstalledPluginFilter extends ViewerFilter {

	private HybridProject project;
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if(project == null ) 
			return false;
		CordovaPluginManager pm = project.getPluginManager();
		if(element instanceof CordovaRegistryPluginInfo ){
			CordovaRegistryPluginInfo plugin = (CordovaRegistryPluginInfo) element;
			return !pm.isPluginInstalled(plugin.getName());
		}
		return false;
	}
	
	public void setProject(HybridProject project){
		this.project = project;
	}

}
