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
package org.eclipse.thym.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.extensions.PlatformSupport;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.PlatformImage;
import org.eclipse.thym.ui.platforms.navigator.internal.HybridPlatformFolder;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class CordovaPlatformWorkbenchAdapter implements IWorkbenchAdapter {

	@Override
	public Object[] getChildren(Object o) {
		return new Object[0];
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		if (object instanceof HybridPlatformFolder) {
			HybridPlatformFolder platform = (HybridPlatformFolder) object;
			if(platform.getPlatform() != null) {
				PlatformSupport platformSupport = HybridCore.getPlatformSupport(platform.getPlatform().getName());
				if(platformSupport != null) {
					return PlatformImage.getIconFor(PlatformImage.ATTR_PLATFORM_SUPPORT, platformSupport.getID());
				}
			}
			return HybridUI.getImageDescriptor(HybridUI.PLUGIN_ID, "icons/obj16/cordova_16.png");
		}
		return null;
	}

	@Override
	public String getLabel(Object o) {
		if (o instanceof HybridPlatformFolder) {
			HybridPlatformFolder platformFolder = (HybridPlatformFolder) o;
			if (platformFolder.getPlatform() != null) {
				return platformFolder.getPlatform().getName() + " " + platformFolder.getPlatform().getSpec();
			} else {
				return platformFolder.getFolder().getName();
			}
		}
		return null;
	}

	@Override
	public Object getParent(Object o) {
		if (o instanceof HybridPlatformFolder) {
			HybridPlatformFolder platform = (HybridPlatformFolder) o;
			return platform.getFolder();
		}
		return null;
	}

}
