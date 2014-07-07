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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public  interface HybridMobileEngineLocator {
	
	/**
	 * Callback listener to report found engines.
	 *
	 */
	public interface EngineSearchListener{
		
		public void libraryFound(PlatformLibrary library);
	}
	
	/**
	 * Searches for local engines. It uses the callback listener to report engines that are found. T
	 * he path contains the absolute path of the folder to search in.
	 * 
	 * @param path
	 * @param listener
	 * @param monitor
	 */
	public void searchForRuntimes(IPath path,  EngineSearchListener listener, IProgressMonitor monitor);

}
