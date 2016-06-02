/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.thym.core.config;

import static org.eclipse.thym.core.config.WidgetModelConstants.PLUGIN_ATTR_NAME;
import static org.eclipse.thym.core.config.WidgetModelConstants.PLUGIN_ATTR_SPEC;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Plugin tag in config.xml
 *
 * @author Angel Misevski
 *
 */
public class Plugin extends AbstractConfigObject {

	private Property<String> name = new Property<String>(PLUGIN_ATTR_NAME);
	private Property<String> spec = new Property<String>(PLUGIN_ATTR_SPEC);

	Plugin(Node node) {
		this.itemNode = (Element) node;
		name.setValue(getNodeAttribute(node, null, PLUGIN_ATTR_NAME));
		spec.setValue(getNodeAttribute(node, null, PLUGIN_ATTR_SPEC));
	}

	public String getName() {
		return name.getValue();
	}

	public String getSpec() {
		return spec.getValue();
	}

	public void setName(String name) {
		setAttributeValue(itemNode, null, PLUGIN_ATTR_NAME, name);
		this.name.setValue(name);
	}

	public void setSpec(String spec) {
		setAttributeValue(itemNode, null, PLUGIN_ATTR_SPEC, spec);
		this.spec.setValue(spec);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Plugin))
			return false;
		if(obj == this )
			return true;
		Plugin that = (Plugin) obj;
		return equalField(that.getName(), this.getName());
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		if(getName() != null )
			hash *= getName().hashCode();
		return hash;
	}
}
