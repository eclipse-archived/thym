/*******************************************************************************
 * Copyright (c) 2014 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 * 		 Zend Technologies Ltd. - initial implementation
 *******************************************************************************/
package org.eclipse.thym.core.engine;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.thym.core.engine.internal.cordova.DownloadableCordovaEngine;

/**
 * @author Wojciech Galanciak, 2014
 *
 */
public abstract class AbstractEngineRepoProvider {

	/**
	 * Returns a list of all {@link DownloadableCordovaEngine}s that are
	 * available from this repository.
	 * 
	 * @return list of Cordova engines
	 * @throws CoreException
	 */
	public abstract List<DownloadableCordovaEngine> getEngines()
			throws CoreException;
	
	/**
	 * Returns a list of all {@link DownloadableCordovaEngine}s with specified id that are
	 * available from this repository.
	 * 
	 * @return list of Cordova engines
	 * @throws CoreException
	 */
	public abstract List<DownloadableCordovaEngine> getEngines(String engineId)
			throws CoreException;

}
