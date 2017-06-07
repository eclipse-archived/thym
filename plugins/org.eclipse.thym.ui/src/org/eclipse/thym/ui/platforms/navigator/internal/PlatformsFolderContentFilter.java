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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.thym.core.platform.PlatformConstants;

public class PlatformsFolderContentFilter extends ViewerFilter {
	
	public PlatformsFolderContentFilter() {
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if(!(element instanceof IFolder) )
			return true;
		IFolder folder = (IFolder) element;
		IContainer parent = folder.getParent();
		if(parent.getName().equals(PlatformConstants.DIR_PLATFORMS) && parent.getProjectRelativePath().segmentCount() == 1){
			return false;
		}
		return true;
	}

}
