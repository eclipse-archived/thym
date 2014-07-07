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

import static org.eclipse.thym.core.config.WidgetModelConstants.AUTHOR_ATTR_EMAIL;
import static org.eclipse.thym.core.config.WidgetModelConstants.AUTHOR_ATTR_HREF;
import static org.eclipse.thym.core.config.WidgetModelConstants.NS_W3C_WIDGET;
import static org.eclipse.thym.core.config.WidgetModelConstants.WIDGET_TAG_AUTHOR;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
/**
 * Author tag on the config.xml
 * 
 * @author Gorkem Ercan
 *
 */
public class Author extends AbstractConfigObject {
	
	private Property<String> href = new Property<String>(AUTHOR_ATTR_HREF);
	private Property<String> email = new Property<String>(AUTHOR_ATTR_EMAIL);
	private Property<String> name = new Property<String>("name");

	Author(Node item) {
		itemNode = (Element)item;
		href.setValue(getNodeAttribute(item, null, AUTHOR_ATTR_HREF));
		email.setValue(getNodeAttribute(item, null,AUTHOR_ATTR_EMAIL));
		name.setValue(item.getTextContent());
	}

	public String getHref() {
		return href.getValue();
	}

	public String getEmail() {
		return email.getValue();
	}

	public String getName() {
		return name.getValue();
	}
	
	public void setHref(String href) {
		this.href.setValue( href );
		setAttributeValue(itemNode, null, "href", href);
	}

	public void setEmail(String email) {
		this.email.setValue(email);
		setAttributeValue(itemNode, null, "email", email);
	}

	public void setName(String name) {
		this.name.setValue(name);
		setTextContentValue(itemNode, name);
	}	
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Author))
			return false;
		if( obj == this )
			return true;
		Author that = (Author) obj;
		return equalField(that.getEmail(),this.getEmail()) &&
				equalField(that.getHref(), this.getHref()) &&
				equalField(that.getName(), this.getName());
	}
	
	@Override
	public int hashCode() {
		int hash= super.hashCode();
		if(getEmail() !=null )
			hash *= getEmail().hashCode();
		if(getHref() != null )
			hash *= getHref().hashCode();
		if(getName() != null )
			hash *= getName().hashCode();
		return hash;
	}



}
