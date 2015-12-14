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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Generic execution result for Cordova CLI.
 * 
 * @author Gorkem Ercan
 *
 */
public class CordovaCLIResult {
	
	private final String message;
	
	public CordovaCLIResult(String message){
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}

	public IStatus asStatus(){
		return Status.OK_STATUS;
	}
	
	public <T extends CordovaCLIResult> T convertTo(Class<T> resultType){
		try {
			return resultType.getConstructor(String.class).newInstance(this.message);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Result type is not valid");
		}
	}

}
