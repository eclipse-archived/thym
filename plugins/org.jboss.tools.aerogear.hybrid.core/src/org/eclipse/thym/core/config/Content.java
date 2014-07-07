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

import static org.eclipse.thym.core.config.WidgetModelConstants.CONTENT_ATTR_ENCODING;
import static org.eclipse.thym.core.config.WidgetModelConstants.CONTENT_ATTR_SRC;
import static org.eclipse.thym.core.config.WidgetModelConstants.CONTENT_ATTR_TYPE;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
/**
 * content tag on the config.xml
 * 
 * @author Gorkem Ercan
 *
 */
public class Content extends AbstractConfigObject {
	
	
	private Property<String> src = new Property<String>("src");
	private Property<String> type = new Property<String>("type");
	private Property<String> encoding = new Property<String>("encoding");
	
	
   Content(Node node ) {
		this.itemNode = (Element)node;
		src.setValue(getNodeAttribute(node, null, CONTENT_ATTR_SRC));
		type.setValue(getNodeAttribute(node, null, CONTENT_ATTR_TYPE));
		encoding.setValue(getNodeAttribute(node, null, CONTENT_ATTR_ENCODING));
	}
	
	public String getSrc() {
		return src.getValue();
	}
	
	public void setSrc(String src) {
		this.src.setValue(src);
		setAttributeValue(itemNode, null, CONTENT_ATTR_SRC, src);
	}

	public String getType() {
		return type.getValue();
	}

	public void setType(String type) {
		this.type.setValue(type);
		setAttributeValue(itemNode, null, CONTENT_ATTR_TYPE, type);	
	}

	public String getEncoding() {
		return encoding.getValue();
	}

	public void setEncoding(String encoding) {
		this.encoding.setValue(encoding);
		setAttributeValue(itemNode, null, CONTENT_ATTR_ENCODING,encoding);	
	}
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Content ))
			return false;
		if(obj == this )
			return true;
		Content that = (Content)obj;
		return equalField(that.getSrc(), this.getSrc()) ;
	}
	
	@Override
	public int hashCode() {
		if(getSrc() != null )
			return getSrc().hashCode();
		return super.hashCode();
	}
	
}
