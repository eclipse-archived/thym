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

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
/**
 * Stream listener for Cordova CLI output. 
 * 
 * @author Gorkem Ercan
 *
 */
public class CordovaCLIStreamListener implements IStreamListener {

	private final StringBuffer message = new StringBuffer();
	
	@Override
	public void streamAppended(String text, IStreamMonitor monitor) {
		message.append(text);
	}

	/**
	 * Returns all the messages returned 
	 * excluding the error messages
	 * @return
	 */
	public String getMessage(){
		return message.toString();
	}

}
