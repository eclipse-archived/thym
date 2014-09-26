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
package org.eclipse.thym.core.config;

import static org.eclipse.thym.core.config.WidgetModelConstants.FEATURE_ATTR_NAME;
import static org.eclipse.thym.core.config.WidgetModelConstants.FEATURE_ATTR_REQUIRED;
import static org.eclipse.thym.core.config.WidgetModelConstants.FEATURE_PARAM_TAG;
import static org.eclipse.thym.core.config.WidgetModelConstants.PARAM_ATTR_NAME;
import static org.eclipse.thym.core.config.WidgetModelConstants.PARAM_ATTR_VALUE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * Feature tag on config.xml
 * 
 * @author Gorkem Ercan
 *
 */
public class Feature extends AbstractConfigObject {
	
	private Property<String> name = new Property<String>(FEATURE_ATTR_NAME);
	private Property<Boolean> required = new Property<Boolean>(FEATURE_ATTR_REQUIRED);
	private Property<HashMap<String, String>> params = new Property<HashMap<String, String>>(FEATURE_PARAM_TAG); 

	
	Feature(Node node){
		this.itemNode = (Element)node;
		name.setValue(getNodeAttribute(node, null, FEATURE_ATTR_NAME));
		required.setValue(Boolean.parseBoolean(getNodeAttribute(node, null, FEATURE_ATTR_REQUIRED)));
		Element el = (Element)node;
		NodeList nodes = el.getElementsByTagName(FEATURE_PARAM_TAG);
		if (nodes.getLength() > 0) {
			HashMap<String, String> props = new HashMap<String, String>(nodes.getLength());
			for (int i = 0; i < nodes.getLength(); i++) {
				String name = getNodeAttribute(nodes.item(i), null, PARAM_ATTR_NAME);
				String value = getNodeAttribute(nodes.item(i), null, PARAM_ATTR_VALUE);
				props.put(name, value);
			}
			params.setValue(props);
		}else{
			params.setValue(null);
		}
	}

	public String getName() {
		return name.getValue();
	}
	
	public boolean getRequired() {
		if(required.getValue() == null ){
			return false;
		}
		return required.getValue();
	}

	public void setRequired(boolean required) {
		setAttributeValue(itemNode, null, FEATURE_ATTR_REQUIRED, Boolean.toString(required));
		this.required.setValue(Boolean.valueOf(required));
	}

	public void setName(String name) {
		setAttributeValue(itemNode, null, FEATURE_ATTR_NAME, name);
		this.name.setValue(name);
	}

	public void addParam(String name, String value){
		HashMap<String, String> props = new HashMap<String, String>();
		if(params.getValue() != null ){ //replace to trigger property change
			props.putAll(params.getValue());
		}
		if(!props.containsKey(name)){
			Document doc = itemNode.getOwnerDocument();
			Element el = doc.createElement( FEATURE_PARAM_TAG);
			el.setAttribute(PARAM_ATTR_NAME, name);
			el.setAttribute(PARAM_ATTR_VALUE, value);
			itemNode.appendChild(el);
		}else{
			Element paramElement = findParamElement(name);
			paramElement.setAttribute(PARAM_ATTR_VALUE, value);
		}
		props.put(name, value);
		params.setValue(props);
	}
	
	public void removeParam(String name ){
		if(!params.getValue().containsKey(name))
			return;
		HashMap<String, String> props = new HashMap<String, String>();
		if(params.getValue() != null ){ //replace to trigger property change
			props.putAll(params.getValue());
		}
		Element paramElement = findParamElement(name);
		if(paramElement != null ){
			itemNode.removeChild(paramElement);
		}
		props.remove(name);
		params.setValue(props);
	}
	
	private Element findParamElement(String name){
		Node child = itemNode.getFirstChild();
		while(child != null ){
			if(child.getNodeType() == Node.ELEMENT_NODE ){
				Element e = (Element)child;
				if(name.equals(e.getAttribute(PARAM_ATTR_NAME))){
					return e;
				}
			}
			child = child.getNextSibling();
		}
		return null;
	}
	
	/**
	 * Returns an unmodifiable copy of parameter map. 
	 * @return
	 */
	public Map<String, String> getParams() {
		if(params.getValue() == null ){
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(params.getValue());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Feature))
			return false;
		if (obj == this ) 
			return true;
		Feature that = (Feature)obj;
		return equalField(that.getName(), this.getName());
	}
	
	@Override
	public int hashCode() {
		if(getName() != null)
			return getName().hashCode();
		return super.hashCode();
	}

}
