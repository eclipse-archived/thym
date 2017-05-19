/*******************************************************************************
 * Copyright (c) 2015, 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 * 		 IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.thym.core.internal.cordova;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.internal.util.ExternalProcessUtility;

/**
 * Wrapper around Cordova CLI. Provides low level 
 * access to Cordova CLI.
 *
 *@author Gorkem Ercan
 *
 */
@SuppressWarnings("restriction")
public class CordovaCLI {
	
	private Map<String,String> additionalEnvProps;
	
	public CordovaCLI(){
		additionalEnvProps = HybridCore.getEnvVariables();
	}
	
	public CordovaCLIResult version(final IProgressMonitor monitor) throws CoreException{
		final CordovaCLIStreamListener streamListener = new CordovaCLIStreamListener();
		IProcess process = startShell(streamListener, monitor, getLaunchConfiguration("cordova -version"));
		String cordovaCommand = "cordova -version\n";
		sendCordovaCommand(process, cordovaCommand, monitor);
		CordovaCLIResult result = new CordovaCLIResult(streamListener.getMessage());
		return result;		
	}
	
	public CordovaCLIResult nodeVersion(final IProgressMonitor monitor) throws CoreException{
		final CordovaCLIStreamListener streamListener = new CordovaCLIStreamListener();
		IProcess process = startShell(streamListener, monitor, getLaunchConfiguration("node -v"));
		String command = "node -v\n";
		sendCordovaCommand(process, command, monitor);
		CordovaCLIResult result= new CordovaCLIResult(streamListener.getMessage());
		return result;
	}

	protected void sendCordovaCommand(final IProcess process, final String cordovaCommand,
			final IProgressMonitor monitor) throws CoreException {
		try {
			final IStreamsProxy streamProxy = process.getStreamsProxy();
			streamProxy.write(cordovaCommand.toString());
			while (!process.isTerminated()) {
				//exit the shell after sending the command
				try{
					streamProxy.write("exit\n");
				} catch (IOException e) {
					if(process.isTerminated()){ //ok, if process is terminated
						break;
					}
					throw e;
				}
				if (monitor.isCanceled()) {
					process.terminate();
					break;
				}
				Thread.sleep(50);
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Fatal error invoking cordova CLI", e));
		} catch (InterruptedException e) {
			HybridCore.log(IStatus.INFO, "Exception waiting for process to terminate", e);
		}
	}
	
	//public visibility to support testing
	public IProcess startShell(final IStreamListener listener, final IProgressMonitor monitor, 
			final ILaunchConfiguration launchConfiguration) throws CoreException{
		ArrayList<String> commandList = new ArrayList<String>();
		if(isWindows()){
			commandList.add("cmd");
		}else{
			commandList.add("/bin/bash");
			commandList.add("-l");
		}
		
		ExternalProcessUtility ep = new ExternalProcessUtility();
		IProcess process = ep.exec(commandList.toArray(new String[commandList.size()]), getWorkingDirectory(), 
				monitor, null, launchConfiguration, listener, listener);
		 return process;
	}
	
	protected boolean isWindows(){
		String OS = System.getProperty("os.name","unknown");
		return OS.toLowerCase().indexOf("win")>-1;
	}
	
	protected ILaunchConfiguration getLaunchConfiguration(String label){
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IExternalToolConstants.ID_PROGRAM_LAUNCH_CONFIGURATION_TYPE);
		try {
			ILaunchConfiguration cfg = type.newInstance(null, "cordova");
			ILaunchConfigurationWorkingCopy wc = cfg.getWorkingCopy();
			wc.setAttribute(IProcess.ATTR_PROCESS_LABEL, label);
			if(additionalEnvProps != null && !additionalEnvProps.isEmpty()){
				wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,additionalEnvProps);
			}
			cfg = wc.doSave();
			return cfg;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected File getWorkingDirectory(){
		return null;
	}
}
