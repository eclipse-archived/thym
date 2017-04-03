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
package org.eclipse.thym.ui.requirement;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.ui.HybridUI;

/**
 * Proxy object for org.eclipse.thym.ui.platformRequirements extension point
 * @author rawagner
 *
 */
public class PlatformRequirementsExtension {
	private static final String ATTR_CLASS = "class";
	private static final String ATTR_PLATFORM_ID= "platformID";
	public static final String EXTENSION_POINT_ID= "org.eclipse.thym.ui.platformRequirements";
	
	private String platformID;
	private PlatformRequirementsHandler handler;
	
	public PlatformRequirementsExtension( final IConfigurationElement configurationElement) {
		try {
			handler = (PlatformRequirementsHandler) configurationElement.createExecutableExtension(ATTR_CLASS);
		} catch (CoreException e) {
			HybridUI.log(IStatus.ERROR, "Platform Requirements Handler can not be instantiated ", e);
		}
		platformID = configurationElement.getAttribute(ATTR_PLATFORM_ID);
	}
	
	public PlatformRequirementsHandler getHandler(){
		if(handler == null ){
			throw new IllegalStateException("A platform requirements handler could not be initiated. See error logs for details.");
		}
		return handler;
	}
	
	public String getPlatformID(){
		return platformID;
	}

}
