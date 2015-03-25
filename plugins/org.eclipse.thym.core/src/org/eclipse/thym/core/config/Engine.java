/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat Inc. - initial API and implementation and/or initial
 * documentation
 *******************************************************************************/
package org.eclipse.thym.core.config;
import static org.eclipse.thym.core.config.WidgetModelConstants.ENGINE_ATTR_NAME;
import static org.eclipse.thym.core.config.WidgetModelConstants.ENGINE_ATTR_VERSION;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Engine tag on config.xml
 * @author Gorkem Ercan
 *
 */
public class Engine extends AbstractConfigObject {
	
	private Property<String> name = new Property<String>(ENGINE_ATTR_NAME);
	private Property<String> version = new Property<String>(ENGINE_ATTR_VERSION);
	
	Engine(Node node){
		itemNode = (Element)node;
		name.setValue(getNodeAttribute(node, null, ENGINE_ATTR_NAME));
		version.setValue(getNodeAttribute(node, null, ENGINE_ATTR_VERSION));
	}
	
	public String getName() {
		return name.getValue();
	}
	
	public void setName(String name) {
		this.name.setValue(name);
		setAttributeValue(itemNode, null, ENGINE_ATTR_NAME, name);
	}

	public String getVersion() {
		return version.getValue();
	}

	public void setVersion(String version) {
		this.version.setValue(version);
		setAttributeValue(itemNode, null, ENGINE_ATTR_VERSION, version);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Engine))
			return false;
		if(obj == this )
			return true;
		Engine that = (Engine) obj;
		return equalField(that.getName(), this.getName()) &&
				equalField(this.getVersion(), that.getVersion());
	}
	
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		if(getName() != null ){
			hash *= getName().hashCode();
		}
		if(getVersion() != null ){
			hash *= getVersion().hashCode();
		}
		return hash;
	}
}
