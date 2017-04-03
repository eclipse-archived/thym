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
package org.eclipse.thym.core.extensions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.core.CordovaEnvVariables;
import org.eclipse.thym.core.HybridCore;

/**
 * Proxy object for org.eclipse.thym.core.cordovaEnvVariables extension point
 * @author rawagner
 *
 */
public class CordovaEnvVariablesExtension {
	
	private static final String ATTR_CLASS = "class";
	public static final String EXTENSION_POINT_ID= "org.eclipse.thym.core.cordovaEnvVariables";
	
	private CordovaEnvVariables handler;
	
	public CordovaEnvVariablesExtension( final IConfigurationElement configurationElement) {
		try {
			handler = (CordovaEnvVariables) configurationElement.createExecutableExtension(ATTR_CLASS);
		} catch (CoreException e) {
			HybridCore.log(IStatus.ERROR, "Cordova Env Variables Handler can not be instantiated ", e);
		}
	}
	
	public CordovaEnvVariables getHandler(){
		if(handler == null ){
			throw new IllegalStateException("A cordova env variables handler could not be initiated. See error logs for details.");
		}
		return handler;
	}
}
