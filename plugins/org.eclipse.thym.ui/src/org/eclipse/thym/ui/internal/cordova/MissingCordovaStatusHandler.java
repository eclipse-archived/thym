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
package org.eclipse.thym.ui.internal.cordova;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.ui.internal.status.DefaultStatusHandler;
import org.eclipse.thym.ui.status.AbstractStatusHandler;

public class MissingCordovaStatusHandler extends AbstractStatusHandler {

	public MissingCordovaStatusHandler() {
	}

	@Override
	public void handle(IStatus status) {
		List<HybridProject> projects=HybridCore.getHybridProjects();
		if(projects.isEmpty()){
			DefaultStatusHandler dsh = new DefaultStatusHandler();
			dsh.handle(status);
			return;
		}
		//Call check requirements for any HybridProject requirement checking 
		//is not project specific.
		RequirementsUtility.checkCordovaRequirements(projects.get(0));
	}

	@Override
	public void handle(CoreException e) {
		handle(e.getStatus());
	}

}
