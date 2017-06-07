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
package org.eclipse.thym.ui.platforms.navigator.internal;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.thym.core.engine.HybridMobileEngine;

public class HybridPlatformFolder extends PlatformObject{
	
	private IFolder folder;
	private HybridMobileEngine platform;
	
	public HybridPlatformFolder(IFolder folder, HybridMobileEngine platform) {
		this.folder = folder;
		this.platform = platform;
	}

	public IFolder getFolder() {
		return folder;
	}
	
	public HybridMobileEngine getPlatform() {
		return platform;
	}
	
	

}
