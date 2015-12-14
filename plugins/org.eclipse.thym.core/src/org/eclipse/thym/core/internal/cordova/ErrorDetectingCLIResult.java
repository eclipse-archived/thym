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

import java.util.Scanner;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridMobileStatus;

public class ErrorDetectingCLIResult extends CordovaCLIResult{
	/**
	 * Generic Cordova CLI error code
	 */
	public static final int ERROR_GENERAL = 500;
	public static final int ERROR_MISSING_PLUGIN_VARIABLE = 501;
	
	private static final String ERROR_PREFIX = "Error:";
	private StringBuffer errorMessage = new StringBuffer();
	private int errorCode = ERROR_GENERAL; 
	
	public ErrorDetectingCLIResult(String message) {
		super(message);
		parseErrors();
	}
	
	public IStatus asStatus(){
		if(errorMessage.length()>0){
			return new HybridMobileStatus(IStatus.ERROR,HybridCore.PLUGIN_ID,errorCode,errorMessage.toString(),null);
		}
		return super.asStatus();
	}
	
	private void parseErrors(){
		final Scanner scanner = new Scanner(getMessage());
		boolean error = false;
		while(scanner.hasNextLine()){
			String line = scanner.nextLine();
			line = line.trim();// remove leading whitespace
			if(line.startsWith(ERROR_PREFIX)){
				error = true;
				errorMessage = errorMessage.append(line.substring(ERROR_PREFIX.length(), line.length()).trim());
			}else{
				if(error){
					errorMessage.append(System.lineSeparator());	
					errorMessage.append(line);
				}
			}
		}
		scanner.close();
	}

}
