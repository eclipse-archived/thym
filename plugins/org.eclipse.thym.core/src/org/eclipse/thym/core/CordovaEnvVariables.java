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
package org.eclipse.thym.core;

import java.util.Map;

/**
 * Interface for additional environment variables for cordova CLI
 * @author rawagner
 *
 */
public interface CordovaEnvVariables {
	
	/**
	 * Returns additional environment variables that should be used with cordova CLI
	 * @return additional environment variables that should be used with cordova CLI
	 */
	Map<String,String> getAdditionalEnvVariables();

}
