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
package org.eclipse.thym.core.engine;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.extensions.PlatformSupport;

public class PlatformLibrary{
	private final String platformId;
	private final IPath location;
	private HybridMobileLibraryResolver resolver;
	
	public PlatformLibrary(String platformId, IPath location){
		Assert.isNotNull(platformId);
		Assert.isNotNull(location);
		this.platformId = platformId;
		this.location = location;
		
	}
	
	public String getPlatformId() {
		return platformId;
	}

	public IPath getLocation() {
		return location;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PlatformLibrary){
			PlatformLibrary that = (PlatformLibrary)obj;
			return platformId.equals(that.getPlatformId()) 
						&& location.equals(that.getLocation());
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return location.hashCode();
	}

	public HybridMobileLibraryResolver getPlatformLibraryResolver(){
        Assert.isNotNull(platformId);
        if(resolver == null){
        	PlatformSupport platform = HybridCore.getPlatformSupport(platformId);
        	if(platform == null ) return null;
        	try {
        		resolver = platform.getLibraryResolver();
        		resolver.init(location);
        	} catch (CoreException e) {
        		HybridCore.log(IStatus.ERROR,"Library resolver creation error ", e);
        		return null;
        	}
        }
        return resolver;
	}
}