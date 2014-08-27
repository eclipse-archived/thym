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
package org.eclipse.thym.android.core.adt;

import java.util.Comparator;

public class AndroidAPILevelComparator implements Comparator<String>{

	@Override
	public int compare(String sdk1, String sdk2) {
		if(sdk1 == null && sdk2 == null ){
			throw new NullPointerException("comparator arguments can not be null");
		}
		char[] l1 = sdk1.toCharArray();
		char[] l2 = sdk2.toCharArray();
		for (int i = 0; i < Math.min(l1.length, l2.length); i++) {
			if(l1[i] > l2[i]){
				return 1;
			}
			if(l1[i] < l2[i]){
				return -1;
			}
		}
		if(l1.length > l2.length ){
			return 1;
		}
		if(l1.length < l2.length ){
			return -1;
		}
		return 0;
	}
	
}