/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.internal;

import org.eclipse.thym.ui.platforms.navigator.internal.HybridPlatformFolder;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class CordovaPlatformProperties implements IPropertySource {
	
	private HybridPlatformFolder source;
	private static IPropertyDescriptor[] descriptors;
	private static final String PLATFORM_NAME="name";
	private static final String PLATFORM_SPEC="spec";
	
	static{
		descriptors = new IPropertyDescriptor[6];
		descriptors[0] = createPropertyDescriptor(PLATFORM_NAME, "Name");
		descriptors[1] = createPropertyDescriptor(PLATFORM_SPEC, "Spec");
	}
	
	public CordovaPlatformProperties(HybridPlatformFolder platform){
		this.source = platform;
	}

	@Override
	public Object getEditableValue() {
		return this;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors;
	}

	@Override
	public Object getPropertyValue(Object id) {
		if(PLATFORM_NAME.equals(id)){
			return source.getPlatform().getName();
		}
		if(PLATFORM_SPEC.equals(id)){
			return source.getPlatform().getSpec();
		}
		return null;
	}

	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {
	}

	@Override
	public void setPropertyValue(Object id, Object value) {
	}
	
	private static IPropertyDescriptor createPropertyDescriptor(String field, String label){
		PropertyDescriptor descriptor = new PropertyDescriptor(field, label);
		descriptor.setCategory("Cordova Platform");
		descriptor.setAlwaysIncompatible(true);
		return descriptor;

		
	}

}
