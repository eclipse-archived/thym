/*******************************************************************************
 * Copyright (c) 2013, 2015 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ios.core.xcode;

import java.util.HashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.thym.core.engine.HybridMobileLibraryResolver;
import org.eclipse.thym.core.platform.PlatformConstants;

/**
 * Resolves iOS cordova distros to files used by the tool.
 * @author Gorkem Ercan
 *
 */
public class IosLibraryResolver extends HybridMobileLibraryResolver {

	private HashMap<String, String> files = new HashMap<String, String>();
	
	private void initFiles() {
		files.put(PlatformConstants.FILE_JS_CORDOVA, "ios/platform_www/cordova.js");
	}

	@Override
	public String getTemplateFile(String key) {
		if(files.isEmpty()){
			initFiles();
		}
		Assert.isNotNull(key);
		return files.get(key);
	}
}