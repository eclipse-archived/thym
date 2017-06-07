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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.thym.ui.platforms.navigator.internal.HybridPlatformFolder;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

public class CordovaPlatformAdapterFactory implements IAdapterFactory {
	
	private static Class<?>[] ADAPTER_LIST= new Class[] {
		IPropertySource.class,
		IResource.class,
		IWorkbenchAdapter.class,
	};

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(!(adaptableObject instanceof HybridPlatformFolder)) 
			return null;
		HybridPlatformFolder platform = (HybridPlatformFolder)adaptableObject;
		if(IPropertySource.class.equals(adapterType)){
			return new CordovaPlatformProperties(platform);
		}
		if(IWorkbenchAdapter.class.equals(adapterType)){
			return new CordovaPlatformWorkbenchAdapter();
		}
		if(IResource.class.equals(adapterType)){
			return platform.getFolder();
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class[] getAdapterList() {
		return ADAPTER_LIST;
	}

}
