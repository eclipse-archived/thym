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
package org.eclipse.thym.ui.internal.status;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.status.AbstractStatusHandler;

public class HybridMobileStatusExtension {
	
	
	private static final String ATTR_PLUGIN = "plugin";
	private static final String ATTR_CLASS = "class";
	private static final String ATTR_CODE = "code";
	public static final String EXTENSION_POINT_ID= "org.eclipse.thym.ui.hybridMobileStatusHandler";

	
	private int code;
	private String plugin;
	private AbstractStatusHandler handler;
	
	public HybridMobileStatusExtension( final IConfigurationElement configurationElement) {
		code = Integer.parseInt(configurationElement.getAttribute(ATTR_CODE));
		try {
			handler = (AbstractStatusHandler) configurationElement.createExecutableExtension(ATTR_CLASS);
		} catch (CoreException e) {
			HybridUI.log(IStatus.ERROR, "Status Handler can not be instantiated ", e);
		}
		plugin = configurationElement.getAttribute(ATTR_PLUGIN);
	}
	
	
	public int getCode(){
		return code;
	}

	
	public String getPluginID(){
		return plugin;
	}
	
	public AbstractStatusHandler getHandler(){
		if(handler == null ){
			throw new IllegalStateException("A status handler could not be initiated. See error logs for details.");
		}
		return handler;
	}


}
