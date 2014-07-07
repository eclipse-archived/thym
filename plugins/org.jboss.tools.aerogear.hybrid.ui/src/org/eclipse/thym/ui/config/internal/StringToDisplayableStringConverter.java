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
package org.eclipse.thym.ui.config.internal;

import org.eclipse.core.databinding.conversion.Converter;

public class StringToDisplayableStringConverter extends Converter {

	public StringToDisplayableStringConverter(){
		super(String.class, String.class);
	}

	@Override
	public Object convert(Object fromObject) {
		if(fromObject == null ){
			throw new IllegalArgumentException("Parameter fromObject was null");
		}
		if(!(fromObject instanceof String)){
			throw new IllegalArgumentException("Parameter fromObject is not a String it is a " + fromObject.getClass().getName());
		}
		//Check if we really need to replace chars first
		String s = (String) fromObject;
		boolean found = false;
		int wsIndex = -1;
		int size = s.length();
		for (int i = 0; i < size; i++) {
			found = Character.isWhitespace(s.charAt(i)) && !Character.isSpaceChar(s.charAt(i));
			if (found) {
				wsIndex = i;
				break;
			}
		}
		if (!found) {
			return s;
		}

		StringBuilder result = new StringBuilder(s.substring(0, wsIndex));
		for (int i = wsIndex + 1; i < size; i++) {
			char ch = s.charAt(i);
			if (!Character.isWhitespace(ch) || Character.isSpaceChar(ch)) {
				result.append(ch);
			}else{
				if(result.length() >0){//skip whitespace before letters
					result.append(' ');
				}
			}
		}
		return result.toString();
	}

}
