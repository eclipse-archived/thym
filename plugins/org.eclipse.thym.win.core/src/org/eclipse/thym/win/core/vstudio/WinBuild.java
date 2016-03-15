/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Red Hat Inc. - initial API and implementation and/or initial documentation
 *		Zend Technologies Ltd. - initial implementation
 *		IBM Corporation - initial API and implementation
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
import org.eclipse.thym.win.core.WinCore;
import org.eclipse.thym.win.internal.core.Messages;

@SuppressWarnings("restriction")
public class WinBuild extends AbstractNativeBinaryBuildDelegate {
	
	public static final String WIN = "windows";
	
	@Override
	public void buildNow(IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			return;
		}
		try {
			monitor.beginTask(Messages.WinBuild_BuildProjectTask, 10);
			SubMonitor generateMonitor = SubMonitor.convert(monitor, 5);
			if (monitor.isCanceled()) {
				return;
			}
			HybridProject hybridProject = HybridProject.getHybridProject(this
					.getProject());
			if (hybridProject == null) {
				throw new CoreException(new Status(IStatus.ERROR,
						WinCore.PLUGIN_ID, Messages.WinBuild_NoHybridError));
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
