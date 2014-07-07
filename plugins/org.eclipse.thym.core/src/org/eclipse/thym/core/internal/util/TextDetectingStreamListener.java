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
package org.eclipse.thym.core.internal.util;

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
/**
 * A utility {@link IStreamListener} implementation that detects if 
 * given text is encountered on the stream.
 * 
 * @author Gorkem Ercan
 *
 */
public class TextDetectingStreamListener implements IStreamListener {

	private boolean detected;
	private String theText;
	/**
	 * Constructor that sets the text to be detected
	 * 
	 * @param text
	 */
	public TextDetectingStreamListener(String text ){
		this.theText = text;
	}
	
	@Override
	public void streamAppended(String text, IStreamMonitor monitor) {
		if(text.contains(theText)){
			detected = true;
		}
	}
	
	public boolean isTextDetected(){
		return detected;
	}

}
