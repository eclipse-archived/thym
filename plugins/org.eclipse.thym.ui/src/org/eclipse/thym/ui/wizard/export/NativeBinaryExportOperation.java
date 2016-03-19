/*******************************************************************************
 * Copyright (c) 2013, 2016 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.wizard.export;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.platform.AbstractNativeBinaryBuildDelegate;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.IOverwriteQuery;

public class NativeBinaryExportOperation extends WorkspaceModifyOperation {

	private List<AbstractNativeBinaryBuildDelegate> delegates;
	private IOverwriteQuery overwriteQuery;
	private File destinationDir;
	
	public NativeBinaryExportOperation(
			List<AbstractNativeBinaryBuildDelegate> delegates,
			File destination,
			IOverwriteQuery query) {
		this.delegates = delegates;
		this.overwriteQuery = query;
		this.destinationDir = destination;
	}

	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
			InvocationTargetException, InterruptedException {
		SubMonitor sm = SubMonitor.convert(monitor);
		sm.setWorkRemaining(delegates.size()*10);
		for (AbstractNativeBinaryBuildDelegate delegate : delegates) {
			if(monitor.isCanceled()){ 
				break; 
			}
			delegate.setRelease(true);
			delegate.buildNow(sm.newChild(10));
			try {
				File buildArtifact = delegate.getBuildArtifact();
				File destinationFile = new File(destinationDir, buildArtifact.getName());
				if(destinationFile.exists()){
					String callback = overwriteQuery.queryOverwrite(destinationFile.toString());
					if(IOverwriteQuery.NO.equals(callback)){
						continue;
					}
					if(IOverwriteQuery.CANCEL.equals(callback)){
						break;
					}
				}
				File artifact = delegate.getBuildArtifact();
				if(artifact.isDirectory()){
					FileUtils.copyDirectoryToDirectory(artifact, destinationDir);
				}else{
					FileUtils.copyFileToDirectory(artifact, destinationDir);
				}
				sm.done();
			} catch (IOException e) {
				HybridCore.log(IStatus.ERROR, "Error on NativeBinaryExportOperation", e);
			}
		}
		monitor.done(); 
	}
}
