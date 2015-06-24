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
package org.eclipse.thym.core.internal.util;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.thym.core.HybridCore;
/**
 * Utilities for calling and processing the output from external executables.
 * 
 * @author Gorkem Ercan
 *
 */
public class ExternalProcessUtility {

	
	public void execAsync ( String commandLine, File workingDirectory, 
			IStreamListener outStreamListener, 
			IStreamListener errorStreamListener, String[] envp) throws CoreException{
		
		HybridCore.trace("Async Execute command line: "+commandLine);
		String[] cmd = DebugPlugin.parseArguments(commandLine);
		Process process =DebugPlugin.exec(cmd, workingDirectory, envp);
		
		
		Launch launch = new Launch(null, "run", null);
		IProcess prcs = DebugPlugin.newProcess(launch, process, "Cordova Plugin:  "+ cmd[0]);
		DebugPlugin.getDefault().getLaunchManager().addLaunch(launch);
		
		
		if(HybridCore.DEBUG){
			outStreamListener = new TracingStreamListener(outStreamListener);
			errorStreamListener = new TracingStreamListener(errorStreamListener);
		}

		if( outStreamListener != null ){
			prcs.getStreamsProxy().getOutputStreamMonitor().addListener(outStreamListener);
		}
		if( errorStreamListener != null ){
			prcs.getStreamsProxy().getErrorStreamMonitor().addListener(errorStreamListener);
		}
	}
	
	public int execSync ( String commandLine, File workingDirectory, 
			IStreamListener outStreamListener, 
			IStreamListener errorStreamListener, IProgressMonitor monitor, String[] envp, ILaunchConfiguration launchConfiguration) throws CoreException{
		
		HybridCore.trace("Sync Execute command line: "+commandLine);
		if(monitor == null ){
			monitor = new NullProgressMonitor();
		}
		String[] cmd = DebugPlugin.parseArguments(commandLine);
		Process process =DebugPlugin.exec(cmd, workingDirectory, envp);
		
		Launch launch = new Launch(launchConfiguration, "run", null);
		IProcess prcs = DebugPlugin.newProcess(launch, process, "Cordova Plugin:  "+ cmd[0]);
		if(launchConfiguration != null){
			DebugPlugin.getDefault().getLaunchManager().addLaunch(launch);
		}
		
		//Set tracing 
		if(HybridCore.DEBUG){
			HybridCore.trace("Creating TracingStreamListeners for " + commandLine);
			outStreamListener = new TracingStreamListener(outStreamListener);
			errorStreamListener = new TracingStreamListener(outStreamListener);
		}
		
		if( outStreamListener != null ){
			prcs.getStreamsProxy().getOutputStreamMonitor().addListener(outStreamListener);
		}

		if( errorStreamListener != null ){
			prcs.getStreamsProxy().getErrorStreamMonitor().addListener(errorStreamListener);
		}
		
		while (!prcs.isTerminated()) {
			try {
				if (monitor.isCanceled()) {
					prcs.terminate();
					break;
				}
				Thread.sleep(50);
			} catch (InterruptedException e) {
				HybridCore.log(IStatus.INFO, "Exception waiting for process to terminate", e);
			}
		}
		return prcs.getExitValue();
	}	
	
	
	
}
