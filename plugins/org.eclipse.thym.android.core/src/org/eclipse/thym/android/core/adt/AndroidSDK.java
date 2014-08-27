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

public class AndroidSDK {
	
	private String id;
	private String type;
	private String apiLevel = "0";

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getApiLevel() {
		return apiLevel;
	}

	public void setApiLevel(String apiLevel) {
		this.apiLevel = apiLevel;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj == null ) return false;
		if( obj == this ) return true;
		if( obj instanceof AndroidSDK) {
			AndroidSDK that = (AndroidSDK)obj;
			return that.getId().equals(this.getId());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}
	

}
