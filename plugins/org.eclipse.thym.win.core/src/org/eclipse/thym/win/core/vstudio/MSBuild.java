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
package org.eclipse.thym.win.core.vstudio;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.internal.cordova.CordovaCLI;
import org.eclipse.thym.core.platform.AbstractNativeBinaryBuildDelegate;
import org.eclipse.thym.win.core.WPCore;
import org.eclipse.thym.win.internal.core.Messages;

/**
 * Wrapper for MSBuild tool on Windows.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */

@SuppressWarnings("restriction")
public class MSBuild extends AbstractNativeBinaryBuildDelegate {
	
	public static final String WIN = "windows";
	
	@Override
	public void buildNow(IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			return;
		}
		try {
			monitor.beginTask(Messages.MSBuild_BuildProjectTask, 10);
			SubMonitor generateMonitor =SubMonitor.convert(monitor,5);
			if (monitor.isCanceled()) {
				return;
			}
			HybridProject hybridProject = HybridProject.getHybridProject(this
					.getProject());
			if (hybridProject == null) {
				throw new CoreException(new Status(IStatus.ERROR,
						WPCore.PLUGIN_ID, Messages.MSBuild_NoHybridError));
			}
			String buildType = "--debug";
			if(isRelease()){
				buildType = "--release";
			}
			CordovaCLI.newCLIforProject(hybridProject).build(generateMonitor, WIN, buildType);
		} finally {
			monitor.done();
		}
	}

}
