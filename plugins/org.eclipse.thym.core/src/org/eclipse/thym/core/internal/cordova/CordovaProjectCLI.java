/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.internal.cordova;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.thym.core.HybridProject;

/**
 * Cordova CLI containing operations for hybrid project
 * @author rawagner
 *
 */
public class CordovaProjectCLI extends CordovaCLI{
	
	public static final String OPTION_NO_SAVE = "--nosave";
	public static final String OPTION_SAVE = "--save";
	private static final String P_COMMAND_PLUGIN = "plugin";
	private static final String P_COMMAND_PLATFORM = "platform";
	private static final String P_COMMAND_PREPARE = "prepare";
	private static final String P_COMMAND_EMULATE = "emulate";
	private static final String P_COMMAND_RUN = "run";
	private static final String P_COMMAND_BUILD = "build";
	
	//Store locks for the projects.
	private static Map<String, Lock> projectLock = Collections.synchronizedMap(new HashMap<String,Lock>());
	private HybridProject project;
	
	public enum Command{
		ADD("add"), 
		REMOVE("remove"),
		UPDATE("update"),
		LIST("list");
		
		private final String cliCommand;
		Command(String cli){
			this.cliCommand = cli;
		}
		public String getCliCommand() {
			return cliCommand;
		}
	}
	
	/**
	 * Initialize a CLI for a {@link HybridProject}.
	 * 
	 * @param project
	 * @return a cli wrapper
	 */
	public static CordovaProjectCLI newCLIforProject(HybridProject project){
		if(project == null ){
			throw new IllegalArgumentException("No project specified");
		}
		return new CordovaProjectCLI(project);
	}
	
	private CordovaProjectCLI(HybridProject project){
		super();
		this.project = project;
	}
	
	public CordovaCLIResult build (final IProgressMonitor monitor, final String...options )throws CoreException{
		final CordovaCLIStreamListener streamListener = new CordovaCLIStreamListener();
		IProcess process = startShell(streamListener, monitor, getLaunchConfiguration("cordova build"));
		String cordovaCommand = generateCordovaCommand(P_COMMAND_BUILD, null, options);
		sendCordovaCommand(process, cordovaCommand, monitor);
		CordovaCLIResult result = new CordovaCLIResult(streamListener.getMessage());
		return result;
	}
	
	public CordovaCLIResult prepare (final IProgressMonitor monitor, final String...options )throws CoreException{
		final CordovaCLIStreamListener streamListener = new CordovaCLIStreamListener();
		IProcess process = startShell(streamListener, monitor, getLaunchConfiguration("cordova prepare "));
		String cordovaCommand = generateCordovaCommand(P_COMMAND_PREPARE, null, options);
		sendCordovaCommand(process, cordovaCommand, monitor);
		CordovaCLIResult result =  new CordovaCLIResult(streamListener.getMessage());
		return result;
	}
	
	public CordovaCLIResult emulate (final IProgressMonitor monitor, final String...options )throws CoreException{
		final CordovaCLIStreamListener streamListener = new CordovaCLIStreamListener();
		IProcess process = startShell(streamListener, monitor, getLaunchConfiguration("cordova emulate"));
		String cordovaCommand = generateCordovaCommand(P_COMMAND_EMULATE, null, options);
		sendCordovaCommand(process, cordovaCommand, monitor);
		CordovaCLIResult result =  new CordovaCLIResult(streamListener.getMessage());
		return result;
	}
	
	public CordovaCLIResult run (final IProgressMonitor monitor, final String...options )throws CoreException{
		final CordovaCLIStreamListener streamListener = new CordovaCLIStreamListener();
		IProcess process = startShell(streamListener, monitor, getLaunchConfiguration("cordova run"));
		String cordovaCommand = generateCordovaCommand(P_COMMAND_RUN, null, options);
		sendCordovaCommand(process, cordovaCommand, monitor);
		CordovaCLIResult result =  new CordovaCLIResult(streamListener.getMessage());
		return result;
	}
	
	public CordovaCLIResult platform (final Command command, final IProgressMonitor monitor, final String... options ) throws CoreException{
		final CordovaCLIStreamListener streamListener = new CordovaCLIStreamListener();
		IProcess process = startShell(streamListener, monitor, getLaunchConfiguration("cordova platform "+ command.getCliCommand()));
		String cordovaCommand = generateCordovaCommand(P_COMMAND_PLATFORM, command, options);
		sendCordovaCommand(process, cordovaCommand, monitor);
		CordovaCLIResult result = new CordovaCLIResult(streamListener.getMessage());
		return result;
	}
	
	public CordovaCLIResult plugin(final Command command, final IProgressMonitor monitor, final String... options) throws CoreException{
		final CordovaCLIStreamListener streamListener = new CordovaCLIStreamListener();
		IProcess process = startShell(streamListener, monitor, getLaunchConfiguration("cordova plugin "+ command.getCliCommand()));
		String cordovaCommand = generateCordovaCommand(P_COMMAND_PLUGIN,command, options);
		sendCordovaCommand(process, cordovaCommand, monitor);
		CordovaCLIResult result = new CordovaCLIResult(streamListener.getMessage());
		return result;
	}
	
	@Override
	protected void sendCordovaCommand(final IProcess process, final String cordovaCommand,
			final IProgressMonitor monitor) throws CoreException {
		Lock lock = projectLock();
		lock.lock();
		try{
			super.sendCordovaCommand(process, cordovaCommand, monitor);
		} finally {
			lock.unlock();
		}
		
		
	}
	
	private Lock projectLock(){
		final String projectName = project.getProject().getName();
		Lock l = projectLock.get(project.getProject().getName());
		if(l == null){
			// Use reentrant locks
			l = new ReentrantLock();
			projectLock.put(projectName, l);
		}
		return l;
	}
	
	@Override
	protected File getWorkingDirectory(){
		final IPath wp = project.getProject().getLocation();
		if(wp == null){
			return null;
		}
		return wp.toFile();
	}
	
	private String generateCordovaCommand(final String command, final Command subCommand, final String... options) {
		StringBuilder builder = new StringBuilder();
		builder.append("cordova");
		if(command != null){
			builder.append(" ");
			builder.append(command);
		}
		if(subCommand != null){
			builder.append(" ");
			builder.append(subCommand.getCliCommand() );
		}
		for (String string : options) {
			if(!string.isEmpty()){
				builder.append(" ");
				builder.append(string);
			}
		}
		builder.append("\n");
		return builder.toString();
	}

}
