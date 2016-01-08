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
package org.eclipse.thym.core.internal.cordova;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.thym.core.HybridProject;
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
	public static int checkCordovaRequirements(HybridProject project) {
		try {
			CordovaCLI cli = CordovaCLI.newCLIforProject(project);
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

}
