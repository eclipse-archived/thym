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
package org.eclipse.thym.core.plugin.actions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.config.Feature;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.core.platform.IPluginInstallationAction;

public class PluginInstallRecordAction implements IPluginInstallationAction{
	
	private final HybridProject project;
	private final String pluginName;
	private final Map<String, String> parameters;
	private final static String[] INSTALL_RECORD_PARAMS = { "id","version","url","installPath"}; 
	
	public PluginInstallRecordAction(HybridProject project, String pluginName, Map<String, String> parameters ) {
		this.project = project;
		this.pluginName = pluginName;
		this.parameters = parameters;
	}
	
	@Override
	public void install() throws CoreException {
		WidgetModel widgetModel = WidgetModel.getModel(project);
		Widget widget = widgetModel.getWidgetForEdit();
		
		Feature feature = getExistingFeature(widget);
		if(feature == null ){
			feature = widgetModel.createFeature(widget);
			feature.setName(pluginName);
			widget.addFeature(feature);
		}
		
		// Remove all the parameters that are related to install record 
		// to avoid dangling parameters if a plugin is installed with a 
		// new set of params.
		for (String key : INSTALL_RECORD_PARAMS) {
			if(feature.getParams().containsKey(key)){
				feature.removeParam(key);
			}
		}
		
		Set<String> keys = parameters.keySet();
		for (String paramName : keys) {
			if(feature.getParams().containsKey(paramName)){
				feature.removeParam(paramName);
			}
			feature.addParam(paramName, parameters.get(paramName));
		}
		widgetModel.save();
	}
	
	private Feature getExistingFeature(Widget widget) {
		List<Feature> features = widget.getFeatures();
		if(features == null ) return null;
		for (Feature feature : features) {
			if(feature.getName().equals(pluginName)){
				return feature;
			}
		}
		return null;
	}

	@Override
	public String[] filesToOverwrite() {
		return new String[0];
	}

	@Override
	public void unInstall() throws CoreException {
		WidgetModel widgetModel = WidgetModel.getModel(project);
		Widget widget = widgetModel.getWidgetForEdit();
		
		Feature feature = getExistingFeature(widget);
		if(feature != null ){
			widget.removeFeature(feature);
		}
		widgetModel.save();
	}
}
