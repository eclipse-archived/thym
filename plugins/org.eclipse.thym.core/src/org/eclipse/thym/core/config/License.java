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

import static org.eclipse.thym.core.config.WidgetModelConstants.LICENSE_ATTR_HREF;
import static org.eclipse.thym.core.config.WidgetModelConstants.NS_W3C_WIDGET;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
/**
 * license tag on config.xml
 * @author Gorkem Ercan
 *
 */
public class License extends AbstractConfigObject {

	private Property<String> href = new Property<String>(LICENSE_ATTR_HREF);
	private Property<String> text = new Property<String>("license");
	
	License(Node node) {
		itemNode = (Element)node;
		text.setValue(node.getTextContent());
		href.setValue(getNodeAttribute(itemNode, null,LICENSE_ATTR_HREF));
	}

	public String getHref() {
		return href.getValue();
	}

	public void setHref(String href) {
		this.href.setValue(href);
		setAttributeValue(itemNode, null, LICENSE_ATTR_HREF, href);
	}

	public String getText() {
		return text.getValue();
	}

	public void setText(String text) {
		this.text.setValue(text);
		setTextContentValue(itemNode, text);	
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof License))
			return false;
		if (obj == this ) 
			return true;
		License that = (License)obj;
		return equalField(that.getText(), this.getText()) && 
				equalField(that.getHref(), this.getHref());
	}
	
	@Override
	public int hashCode() {
		int hash = super.hashCode(); 
		if(getHref() != null )
			hash *= getHref().hashCode();
		if(getText() != null )
			hash *= getText().hashCode();
		return hash;
	}
	
}
