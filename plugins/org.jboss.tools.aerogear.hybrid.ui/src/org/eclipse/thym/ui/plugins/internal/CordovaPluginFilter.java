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

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.equinox.internal.p2.ui.discovery.util.PatternFilter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPluginInfo;

@SuppressWarnings("restriction")
public class CordovaPluginFilter extends PatternFilter {

	@Override
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		if( !(element instanceof CordovaRegistryPluginInfo) )
			return false;
		CordovaRegistryPluginInfo pluginInfo = (CordovaRegistryPluginInfo) element;
		if(wordMatches(pluginInfo.getName())){
			return true;
		}
		List<String> keywords = pluginInfo.getKeywords();
		if (keywords != null && !keywords.isEmpty()) {
			for (String keyword : keywords) {
				if (wordMatches(keyword)) {
					return true;
				}
			}
		}
		if(wordMatches(pluginInfo.getDescription())){
			return true;
		}
		Map<String, String> maintainers = pluginInfo.getMaintainers();
		if (maintainers != null && !maintainers.isEmpty()) {
			Set<String> keys = maintainers.keySet();
			for (String key : keys) {
				if (wordMatches(key) || wordMatches(maintainers.get(key))) {
					return true;
				}
			}}
		return false;
		
	}
}
