/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.core.internal.cordova;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.thym.core.CordovaEnvVariables;

public class EnvironmentPropsExt implements CordovaEnvVariables{
	
	public static final String ENV_KEY="ENV_KEY";
	public static final String ENV_VALUE="ENV_VALUE";

	@Override
	public Map<String, String> getAdditionalEnvVariables() {
		Map<String,String> props = new HashMap<>();
		props.put("ENV_KEY", "ENV_VALUE");
		return props;
	}

}
