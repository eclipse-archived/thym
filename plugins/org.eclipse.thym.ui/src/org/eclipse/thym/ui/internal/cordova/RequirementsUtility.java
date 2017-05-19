/*******************************************************************************
 * Copyright (c) 2016, 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.internal.cordova;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.thym.core.internal.cordova.CordovaCLI;
import org.eclipse.thym.core.internal.cordova.CordovaCLIErrors;
import org.eclipse.thym.core.internal.cordova.ErrorDetectingCLIResult;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Version;

public class RequirementsUtility {
	
	/**
	 * Minimum required Cordova CLI version
	 */
	public static final String MIN_CORDOVA_VERSION = "5.2.0";
	
	/**
	 * Checks if the Cordova requirements are available. 
	 * Returns one of the error codes from {@link CordovaCLIErrors}.
	 * 
	 * @param project
	 * @return error code or 0
	 */
	private static int doCheckCordovaRequirements() {
		try {
			CordovaCLI cli = new CordovaCLI();
			ErrorDetectingCLIResult cordovaResult = cli.version(new NullProgressMonitor())
					.convertTo(ErrorDetectingCLIResult.class);
			IStatus cordovaStatus = cordovaResult.asStatus();
			if (cordovaStatus.isOK()) {
				return 0;
			}
			if (cordovaStatus.getCode() == CordovaCLIErrors.ERROR_COMMAND_MISSING) {
				// check if node.js is missing as well
				IStatus nodeStatus = cli.nodeVersion(new NullProgressMonitor())
						.convertTo(ErrorDetectingCLIResult.class)
						.asStatus();
				if(nodeStatus.getCode() == CordovaCLIErrors.ERROR_COMMAND_MISSING){
					return CordovaCLIErrors.ERROR_NODE_COMMAND_MISSING;
				}
				return CordovaCLIErrors.ERROR_CORDOVA_COMMAND_MISSING;
			}
			
			Version cVer = Version.parseVersion(cordovaResult.getMessage());
			Version mVer = Version.parseVersion(MIN_CORDOVA_VERSION);
			if(cVer.compareTo(mVer) < 0 ){
				return CordovaCLIErrors.ERROR_CORDOVA_VERSION_OLD;
			}
			return 0;
			
		} catch (CoreException e) {
			return CordovaCLIErrors.ERROR_GENERAL;
		}
	}
	
	/**
	 * Checks if the required cordova software is installed on the system. 
	 * If there are missing requirements, it prompts UI for missing 
	 * requirements. Returns true if the requirements are installed during
	 * this call.
	 * 
	 * @param project
	 * @return
	 */
	public static boolean checkCordovaRequirements(){
		int status = doCheckCordovaRequirements();
		if(status > 0 ){
			String message = null;
			switch (status) {
			case CordovaCLIErrors.ERROR_CORDOVA_COMMAND_MISSING:
				message = "Cordova is missing on your system. Please refer to <a>instructions</a> on how to install Cordova";
				break;
			case CordovaCLIErrors.ERROR_NODE_COMMAND_MISSING:
				message ="Node.js and Cordova is missing on your system.  Please refer to <a>instructions</a> on how to install required software";
				break;
			default:
				message = "There are missing requirements on your system. Please refer to <a>instructions</a> on how to install required software";
				break;
			}
			MissingRequirementsDialog mrd = new MissingRequirementsDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
			mrd.setMessage(message);
			mrd.open();
			return doCheckCordovaRequirements() == 0;
		}
		return true;
	}

}
