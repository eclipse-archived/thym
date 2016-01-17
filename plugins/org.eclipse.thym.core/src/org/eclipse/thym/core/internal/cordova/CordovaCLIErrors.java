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
package org.eclipse.thym.core.internal.cordova;

public interface CordovaCLIErrors {

	/**
	 * Generic Cordova CLI error code
	 */
	public static final int ERROR_GENERAL = 500;
	// missing requirements
	/**
	 * Missing process/program. This usually indicates a 
	 * required application is not installed on the host 
	 * system.
	 */
	public static final int ERROR_COMMAND_MISSING= 501;
	/**
	 *  Used by {@link RequirementsUtility} to indicate a 
	 *  missing cordova binary.
	 * 
	 */
	public static final int ERROR_CORDOVA_COMMAND_MISSING= 502;
	/**
	 * Used by {@link RequirementsUtility} to indicate missing 
	 * node.js
	 */
	public static final int ERROR_NODE_COMMAND_MISSING= 503;
	/**
	 * Used by {@link RequirementsUtility} to indicate older 
	 * than required cordova version.
	 */
	public static final int ERROR_CORDOVA_VERSION_OLD = 504;
	
	//Errors related to cordova plug-in management
	public static final int ERROR_MISSING_PLUGIN_VARIABLE = 550;


}
