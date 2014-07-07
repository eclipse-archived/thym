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
package org.eclipse.thym.core.platform;

import org.eclipse.core.runtime.CoreException;

public interface IPluginInstallationAction {
	
	/** 
	 * Returns a list of files that will be overwritten 
	 * by the completion of this action.
	 *
	 * @return path(s) representing the files to be overwritten
	 */
	public String[] filesToOverwrite();
	public void install() throws CoreException;
	public void unInstall() throws CoreException;

}
