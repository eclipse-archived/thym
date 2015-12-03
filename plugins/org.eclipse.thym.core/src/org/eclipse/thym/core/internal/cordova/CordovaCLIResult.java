/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. 
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
import org.eclipse.core.runtime.Status;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridMobileStatus;

/**
 * Generic execution result for Cordova CLI.
 * 
 * @author Gorkem Ercan
 *
 */
public class CordovaCLIResult {
	
	private final String errorMessage;
	private final String message;
	
	public CordovaCLIResult(String error, String message){
		this.errorMessage = error;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	String getErrorMessage() {
		return errorMessage;
	}


	boolean hasError() {
		return errorMessage != null && !errorMessage.isEmpty();
	}
	
	IStatus asStatus(){
		if(hasError()){
			return new HybridMobileStatus(IStatus.ERROR, HybridCore.PLUGIN_ID, 500, getErrorMessage(), null);
		}
		return Status.OK_STATUS;
	}
	
	CoreException asCoreException(){
		if(hasError()){
			return new CoreException(asStatus());
		}
		return null;
	}

}
