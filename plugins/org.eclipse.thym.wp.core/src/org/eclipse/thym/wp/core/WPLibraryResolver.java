/*******************************************************************************
 * Copyright (c) 2014 Zend Technologies Ltd. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 * 		 Zend Technologies Ltd. - initial implementation
 *******************************************************************************/
package org.eclipse.thym.wp.core;

import java.util.HashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.thym.core.engine.HybridMobileLibraryResolver;
import org.eclipse.thym.core.platform.PlatformConstants;
/**
 * Implementation of {@link HybridMobileLibraryResolver} for Windows Phone 8
 * platform.
 * 
 * @author Wojciech Galanciak, 2014
 * 
 */
public class WPLibraryResolver extends HybridMobileLibraryResolver {

	private HashMap<String, String> files = new HashMap<String, String>();
	
	@Override
	public String getTemplateFile(String key) {
		if (files.isEmpty()) {
			initFiles();
		}
		Assert.isNotNull(key);
		return files.get(key);
	}

	private void initFiles() {
		files.put(PlatformConstants.FILE_JS_CORDOVA, "wp8/platform_www/cordova.js");
	}

}