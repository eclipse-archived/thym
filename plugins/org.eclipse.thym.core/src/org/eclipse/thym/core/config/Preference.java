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

import static org.eclipse.thym.core.config.WidgetModelConstants.PREFERENCE_ATTR_NAME;
import static org.eclipse.thym.core.config.WidgetModelConstants.PREFERENCE_ATTR_READONLY;
import static org.eclipse.thym.core.config.WidgetModelConstants.PREFERENCE_ATTR_VALUE;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
/**
 * Preference tag on config.xml
 * 
 * @author Gorkem Ercan
 *
 */
public class Preference extends AbstractConfigObject {
	
	private Property<String> name = new Property<String>(PREFERENCE_ATTR_NAME);
	private Property<String> value = new Property<String>(PREFERENCE_ATTR_VALUE);
	private Property<Boolean> readonly = new Property<Boolean>(PREFERENCE_ATTR_READONLY);
	
	Preference(Node node) {
		itemNode = (Element)node;
		name.setValue(getNodeAttribute(node, null, PREFERENCE_ATTR_NAME));
		value.setValue(getNodeAttribute(node, null, PREFERENCE_ATTR_VALUE));
		readonly.setValue(Boolean.parseBoolean(getNodeAttribute(node, null, PREFERENCE_ATTR_READONLY)));
	}

	public String getName() {
		return name.getValue();
	}

	public boolean getReadonly() {
		if (readonly.getValue() == null)
			return false;
		return readonly.getValue().booleanValue(); 
		
	}

	public void setReadonly(boolean readonly) {
		this.readonly.setValue(Boolean.valueOf(readonly));
		setAttributeValue(itemNode, null, PREFERENCE_ATTR_READONLY, Boolean.toString(readonly));
	}

	public void setName(String name) {
		this.name.setValue(name);
		setAttributeValue(itemNode, null, PREFERENCE_ATTR_NAME, name);
	}

	public void setValue(String value) {
		this.value.setValue(value);
		setAttributeValue(itemNode, null, PREFERENCE_ATTR_VALUE, value);
	}

	public String getValue() {
		return value.getValue();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Preference ))
			return false;
		if(obj == this )
			return true;
		Preference that = (Preference)obj;
		return equalField(that.getName(), this.getName()) && 
				equalField(that.getValue(), this.getValue());
	}		   
	
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		if(getName() != null )
			hash *= getName().hashCode();
		if(getValue() != null )
			hash *=getValue().hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return "Preference[name:"+getName()+" value:"+getValue()+ " readonly:"+getReadonly()+ "]";
	}
}
