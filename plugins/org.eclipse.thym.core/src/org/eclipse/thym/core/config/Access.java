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

import static org.eclipse.thym.core.config.WidgetModelConstants.ACCESS_ATTR_BROWSER_ONLY;
import static org.eclipse.thym.core.config.WidgetModelConstants.ACCESS_ATTR_ORIGIN;
import static org.eclipse.thym.core.config.WidgetModelConstants.ACCESS_ATTR_SUBDOMAINS;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Access tag on the config.xml.
 * 
 * @author Gorkem Ercan
 *
 */
public class Access extends AbstractConfigObject{
	
	private Property<String> origin = new Property<String>(WidgetModelConstants.ACCESS_ATTR_ORIGIN);
	private Property<Boolean> subdomains = new Property<Boolean>(WidgetModelConstants.ACCESS_ATTR_SUBDOMAINS);
	private Property<Boolean> browserOnly = new Property<Boolean>(WidgetModelConstants.ACCESS_ATTR_BROWSER_ONLY);
	
	Access(Node node){
		itemNode = (Element)node;
		origin.setValue(getNodeAttribute(node,null, ACCESS_ATTR_ORIGIN));
		String str = getNodeAttribute(node, null, ACCESS_ATTR_SUBDOMAINS);
		if(str != null && !str.isEmpty()){
			subdomains.setValue(Boolean.parseBoolean(str));
		}
		str = getNodeAttribute(node, null, ACCESS_ATTR_BROWSER_ONLY);
		if(str != null && !str.isEmpty()){
			browserOnly.setValue(Boolean.parseBoolean(str));
		}
	}

	public String getOrigin() {
		return origin.getValue();
	}

	public boolean isSubdomains() {
		if(subdomains.getValue() == null)
			return false;
		return subdomains.getValue().booleanValue();
	}

	public boolean isBrowserOnly() {
		if(browserOnly.getValue() == null )
			return false;
		return browserOnly.getValue().booleanValue();
	}
	
	public void setSubdomains(boolean subdomains) {
		this.subdomains.setValue(Boolean.valueOf(subdomains));
		setAttributeValue(itemNode, null, ACCESS_ATTR_SUBDOMAINS, Boolean.toString(subdomains));
	}


	public void setBrowserOnly(boolean browserOnly) {
		this.browserOnly.setValue(browserOnly);
		setAttributeValue(itemNode, null, ACCESS_ATTR_BROWSER_ONLY, Boolean.toString(browserOnly));
	}

	public void setOrigin(String origin) {
		this.origin.setValue(origin);
		setAttributeValue(itemNode, null, ACCESS_ATTR_ORIGIN, origin);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Access))
			return false;
		if(obj == this )
			return true;
		Access that = (Access) obj;
		return equalField(that.getOrigin(), this.getOrigin());
	}
	
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		if(getOrigin() != null )
			hash *= getOrigin().hashCode();
		return hash;
	}
}
