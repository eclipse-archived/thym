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
package org.eclipse.thym.ios.core.simulator;

import static org.eclipse.thym.core.internal.util.FileUtils.directoryCopy;
import static org.eclipse.thym.core.internal.util.FileUtils.toURL;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.thym.ios.core.IOSCore;
import org.eclipse.thym.core.internal.util.ExternalProcessUtility;
import org.osgi.framework.Bundle;
/**
 * Wrapper around the native binary for controlling the iOS Simulator.
 * 
 * @author Gorkem Ercan
 *
 */
public class IOSSimulator {

	private static boolean iosSimCopied =false;
	private File iosSim;
	private boolean tall;
	private boolean retina;
	private boolean is64bit;
	private String family;
	private String[] environment;
	private String pathToBinary;
	private String sdkVersion;

	public IOSSimulator(){
		try {
			Bundle bundle = IOSCore.getContext().getBundle();
			File bundleDataDirectory = bundle.getDataFile("/");			
			iosSim = new File(bundleDataDirectory, "ios-sim");
			URL iosSimBinary = bundle.getEntry("/ios-sim");
			if(!iosSimCopied){
				directoryCopy(iosSimBinary,toURL( bundleDataDirectory));
				iosSimCopied = true;
			}
			if (iosSim.exists() && !iosSim.canExecute()){
				iosSim.setExecutable(true, false);
			}
		} catch (IOException e) {
			IOSCore.log(IStatus.ERROR, "IO error when copying the ios-sim", e);
		}
	}
	
	public void launch() throws CoreException{
		if(iosSim == null || !iosSim.exists() ){
			throw newException(IStatus.ERROR,"ios-sim binary is not extracted correctly");
		}
		StringBuilder cmdLine = new StringBuilder();
		cmdLine.append("\"").append(iosSim.getPath()).append("\" launch ");
		
		assert pathToBinary != null: "Path to the app binary to launch on the simulator is missing"; 
		cmdLine.append("\"").append(pathToBinary).append("\"");
		if( family != null && !family.isEmpty() ){
			cmdLine.append(" --family ").append(family);
		}
		if( sdkVersion != null ){
			cmdLine.append(" --sdk ").append(sdkVersion);
		}
		if(retina){
			cmdLine.append(" --retina");
		}
		if(tall){
			cmdLine.append(" --tall");
		}
		if(is64bit){
			cmdLine.append(" --64bit");
		}
		ExternalProcessUtility processUtility = new ExternalProcessUtility();
		processUtility.execAsync(cmdLine.toString(), iosSim.getParentFile(), null, null,environment);
	}

	public void setTall(boolean tall) {
		this.tall = tall;
	}

	public void setRetina(boolean retina) {
		this.retina = retina;
	}
	
	public void set64bit(boolean setbit){
		this.is64bit = setbit;
	}

	public void setFamily(String family) {
		this.family = family;
	}
	
	public void setSdkVersion(String version){
		this.sdkVersion = version;
	}
	
	private CoreException newException(int severity, String message ){
		return new CoreException(new Status(severity,IOSCore.PLUGIN_ID,message));
	}
	
	/**
	 * The environment variables set in the process
	 * @param envp
	 */
	public void setProcessEnvironmentVariables(String[] envp) {
		this.environment = envp;
		
	}

	public void setPathToBinary(String pathToBinary) {
		this.pathToBinary = pathToBinary;
	}
}
