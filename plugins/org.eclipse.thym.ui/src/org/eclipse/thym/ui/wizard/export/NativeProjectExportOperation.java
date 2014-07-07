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

package org.eclipse.thym.ui.wizard.export;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.thym.core.platform.AbstractProjectGeneratorDelegate;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.IOverwriteQuery;

public class NativeProjectExportOperation extends WorkspaceModifyOperation {

	private List<AbstractProjectGeneratorDelegate> generators;
	private MultiStatus status;
	private IOverwriteQuery overwriteCall;
	

	public NativeProjectExportOperation( List<AbstractProjectGeneratorDelegate> delegates , IOverwriteQuery overwriteCall ){
		this.generators = delegates;
		this.overwriteCall = overwriteCall;
		status = new MultiStatus(HybridUI.PLUGIN_ID,IStatus.OK, "", null);
	}
	
	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
	InvocationTargetException, InterruptedException {
		try{
			int totalWork = generators.size();
			monitor.beginTask("Generate Native Projects", totalWork);
			boolean overwriteAll = false;
			boolean noOverwrite =false;
			for (AbstractProjectGeneratorDelegate generator : generators) {
				if(monitor.isCanceled())
					return;
				File destination = generator.getDestination();
				if(overwriteCall != null && destination.exists() && !overwriteAll){
					if(noOverwrite){// No need to ask just move to next one
						monitor.worked(1);
						continue;
					}
					String callback = overwriteCall.queryOverwrite(destination.toString());
					if(IOverwriteQuery.NO.equals(callback)  ){
						monitor.worked(1);
						continue;
					}
					if(IOverwriteQuery.NO_ALL.equals(callback)){
						monitor.worked(1);
						noOverwrite = true;
						continue;
					}
					if(IOverwriteQuery.ALL.equals(callback)){
						overwriteAll = true;
					}
					if(IOverwriteQuery.CANCEL.equals(callback)){
						return;
					}
				}

				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
				runSingle(subMonitor, generator);
			}
		}finally{
			monitor.done();
		}
	}

	private void runSingle(IProgressMonitor monitor,
			AbstractProjectGeneratorDelegate generator) {
		try{
			generator.generateNow(monitor);
		}catch(CoreException e){
			addToStatus(e);
		}
	}
	
	private void addToStatus(CoreException e){
		IStatus status = e.getStatus();
		String message = e.getLocalizedMessage();
		if(message == null || message.isEmpty()){
			message = "Error during native project export operation";
			status = new Status(IStatus.ERROR, HybridUI.PLUGIN_ID, message,e);
		}
		this.status.add(status);
		
	}
	
	public MultiStatus getStatus() {
		return status;
	}
}
