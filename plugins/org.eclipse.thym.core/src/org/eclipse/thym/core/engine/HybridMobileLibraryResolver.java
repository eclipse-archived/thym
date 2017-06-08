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

public abstract class HybridMobileLibraryResolver {
	
	/**
	 * Returns the URL of the file requested from engine. Destination 
	 * must be a relative path on the target platform's project structure.
	 * May return null if a corresponding file can not be found on the 
	 * engine. 
	 * 
	 * @param destination relative path on target structure
	 * @return URL to the corresponding file on the engine or null
	 */
	public abstract String getTemplateFile(String key);
	
}
