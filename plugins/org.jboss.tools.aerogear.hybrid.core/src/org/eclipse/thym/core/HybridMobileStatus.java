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
package org.eclipse.thym.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class HybridMobileStatus extends Status {
	
	public static final int STATUS_CODE_MISSING_ENGINE = 100;
	public static final int STATUS_CODE_CONFIG_PARSE_ERROR = 423;
	private IProject project;


	public HybridMobileStatus(int severity, String pluginId, int code,
			String message, Throwable exception) {
		super(severity, pluginId, code, message, exception);
	}
	
	public static HybridMobileStatus newMissingEngineStatus(IProject project, String message){
		HybridMobileStatus status =  new HybridMobileStatus(IStatus.ERROR, HybridCore.PLUGIN_ID, STATUS_CODE_MISSING_ENGINE, message, null);
		if(project != null){
			status.setProject(project);
		}
		return status;
	}

	public IProject getProject() {
		return project;
	}
	
	public void setProject(IProject project) {
		this.project = project;
	}
}
