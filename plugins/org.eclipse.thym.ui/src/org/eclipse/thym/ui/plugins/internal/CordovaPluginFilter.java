/*******************************************************************************
 * Copyright (c) 2013, 2015 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.plugins.internal;

import org.eclipse.equinox.internal.p2.ui.discovery.util.PatternFilter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPluginInfo;

@SuppressWarnings("restriction")
public class CordovaPluginFilter extends PatternFilter {

	@Override
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		if( (element instanceof CordovaRegistryPluginInfo) ){
			CordovaRegistryPluginInfo pluginInfo = (CordovaRegistryPluginInfo) element;
			if(wordMatches(pluginInfo.getName()) || wordMatches(pluginInfo.getDescription()) ){
				return true;
			}
		}
		return false;
	}
}
