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
package org.eclipse.thym.core.internal.util;

/**
 * Utilities for working with engines
 * @author rawagner
 *
 */
public class EngineUtils {
	
	/**
	 * Cordova uses semver, spec can therefore starts with ~ or ^
	 * @param spec
	 * @return
	 */
	public static String getExactVersion(String spec){
		if (spec.startsWith("~") || spec.startsWith("^")) {
			return spec.substring(1);
		}
		return spec;
	}

}
