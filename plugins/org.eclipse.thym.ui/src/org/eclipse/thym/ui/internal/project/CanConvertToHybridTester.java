/*******************************************************************************
 * Copyright (c) 2013, 2015 Red Hat, Inc. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat Inc. - initial API and implementation and/or initial
 * documentation
 *******************************************************************************/
package org.eclipse.thym.ui.internal.project;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.platform.PlatformConstants;

public class CanConvertToHybridTester extends PropertyTester {
	
	private static final String PROPERTY_CAN_CONVERT ="canConvertToThym";

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if(PROPERTY_CAN_CONVERT.equals(property)){
			IProject project = (IProject) Platform.getAdapterManager().getAdapter(receiver, IProject.class);
			if(project != null){
				return canConvert(project);
			}
		}
		return false;
	}
	/**
	 * Checks if a project can be converted to {@link HybridProject}.
	 * 
	 * @param project
	 * @return
	 */
    public static boolean canConvert(IProject project){
        boolean configExist = false;
        for(IPath path: PlatformConstants.CONFIG_PATHS){
            IFile config = project.getFile(path);
            if(config.exists()){
                configExist = true;
                break;
            }
        }
        IFolder wwwFile = project.getFolder(PlatformConstants.DIR_WWW);
        return configExist && wwwFile.exists();
    }

}
