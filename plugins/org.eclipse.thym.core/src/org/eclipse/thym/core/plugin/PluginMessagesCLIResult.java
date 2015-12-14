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
package org.eclipse.thym.core.plugin;

import java.util.Scanner;
import java.util.regex.MatchResult;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridMobileStatus;
import org.eclipse.thym.core.internal.cordova.ErrorDetectingCLIResult;

public class PluginMessagesCLIResult extends ErrorDetectingCLIResult {

	private HybridMobileStatus pluginStatus;
	public PluginMessagesCLIResult(String message) {
		super(message);
		parseMessage();
	}
	
	@Override
	public IStatus asStatus() {
		IStatus status = super.asStatus();
		if(status.getSeverity() == IStatus.ERROR){
			return status;
		}
		if(pluginStatus != null){
			return pluginStatus;
		}
		return super.asStatus();
	}
	
	private void parseMessage(){
		
		Scanner scanner = new Scanner(getMessage());
		
		while(scanner.hasNextLine()){
			//check of --variable APP_ID=value is needed
			if( scanner.findInLine("(?:\\s\\-\\-variable\\s(\\w*)=value)") != null ){
				MatchResult mr = scanner.match();
				StringBuilder missingVars = new StringBuilder();
				for(int i = 0; i<mr.groupCount();i++){
					if(i>0){
						missingVars.append(",");
					}
					missingVars.append(mr.group());
				}
				pluginStatus = new HybridMobileStatus(IStatus.ERROR, HybridCore.PLUGIN_ID, ERROR_MISSING_PLUGIN_VARIABLE,
						NLS.bind("This plugin requires {0} to be defined",missingVars), null);
			
			}
			scanner.nextLine();
		}
		scanner.close();
		
	}

}
