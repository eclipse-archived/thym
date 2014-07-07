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
package org.eclipse.thym.ui.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.thym.core.plugin.CordovaPlugin;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

public class CordovaPluginAdapterFactory implements IAdapterFactory {
	
	private static Class<?>[] ADAPTER_LIST= new Class[] {
		IPropertySource.class,
		IResource.class,
		IWorkbenchAdapter.class,
	};

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(!(adaptableObject instanceof CordovaPlugin)) 
			return null;
		CordovaPlugin plugin = (CordovaPlugin)adaptableObject;
		if(IPropertySource.class.equals(adapterType)){
			return new CordovaPluginProperties(plugin);
		}
		if(IWorkbenchAdapter.class.equals(adapterType)){
			return new CordovaPluginWorkbenchAdapter();
		}
		if(IResource.class.equals(adapterType)){
			return plugin.getFolder();
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class[] getAdapterList() {
		return ADAPTER_LIST;
	}

}
