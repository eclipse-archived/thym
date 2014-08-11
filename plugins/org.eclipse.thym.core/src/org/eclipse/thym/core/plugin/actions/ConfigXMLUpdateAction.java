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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.config.Feature;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.core.internal.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfigXMLUpdateAction extends XMLConfigFileAction {
	
	private final HybridProject project;

	public ConfigXMLUpdateAction(HybridProject project, String parent, String value) {
		super(project.getConfigFile().getLocation().toFile(), parent,value);
		this.project = project;
	}

	@Override
	public void install() throws CoreException{
		Element featureNode = getInjectedFeatureNode();
		WidgetModel widgetModel = WidgetModel.getModel(project);
		if(featureNode == null ){
			// let parent handle it
			// We do not want to limit what plugins can insert into config.xml with our Widget model
			// because our widget model may not be supporting the latest and greatest config.xml extensions
			// direct xml injection that super uses does not have this problem.
			super.install();
			// now invite widget model to sync its underlying model with the directly 
			// injected changes.
			widgetModel.resyncModel();
		}else{
			Widget widget = widgetModel.getWidgetForEdit();
			
			Feature feature = getExistingFeature(featureNode, widget);
			if(feature == null ){
				feature = widgetModel.createFeature(widget);
				feature.setName(featureNode.getAttribute("name"));
				String required = featureNode.getAttribute("required");
				boolean isRequired = Boolean.parseBoolean(required);
				if(isRequired) {
					feature.setRequired(isRequired);
				}
				widget.addFeature(feature);
			}
			NodeList nodes = featureNode.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node currNode = nodes.item(i);
				if(currNode.getNodeName().equals("param")){
					Element el = (Element)currNode;
					feature.addParam(el.getAttribute("name"), el.getAttribute("value"));
				}
			}
			widgetModel.save();
		}
	}

	private Element getInjectedFeatureNode() throws CoreException {
		Document doc = XMLUtil.loadXML(xml);
		Element element = doc.getDocumentElement();
		NodeList featureNodes = element.getElementsByTagName("feature");
		Element featureNode = null;
		if(featureNodes.getLength() == 1){
			featureNode = (Element) featureNodes.item(0);
		}
		return featureNode;
	}

	private Feature getExistingFeature(Element element, Widget widget) {
		String featureName = element.getAttribute("name");
		List<Feature> features = widget.getFeatures();
		if(features == null ) return null;
		for (Feature feature : features) {
			if(feature.getName().equals(featureName)){
				return feature;
			}
		}
		return null;
	}

	@Override
	public void unInstall() throws CoreException {
		Element featureNode = getInjectedFeatureNode();
		if(featureNode == null ){// let parent handle it
			super.install();
		}else{
			WidgetModel widgetModel = WidgetModel.getModel(project);
			Widget widget = widgetModel.getWidgetForEdit();
			
			Feature feature = getExistingFeature(featureNode, widget);
			if(feature != null ){
				widget.removeFeature(feature);
			}
			widgetModel.save();
		}
	}

}
