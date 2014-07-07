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
package org.eclipse.thym.ios.core.xcode;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.thym.core.plugin.actions.CopyFileAction;
import org.eclipse.thym.ios.core.IOSCore;
import org.eclipse.thym.ios.core.pbxproject.PBXFile;
import org.eclipse.thym.ios.core.pbxproject.PBXProject;
import org.eclipse.thym.ios.core.pbxproject.PBXProjectException;

public class IOSResourceFileAction extends CopyFileAction {
	
	private final File pbxFile;
	private final String path;

	public IOSResourceFileAction(File source, File target, File pbx, String path) {
		super(source, target);
		this.pbxFile = pbx;
		this.path = path;
	}
	@Override
	public void install() throws CoreException {
		PBXProject project = new PBXProject(pbxFile);
		PBXFile file = new PBXFile(path);
		try {
			project.addResourceFile(file);
			project.persist();
		} catch (PBXProjectException e) {
			throw new CoreException(new Status(IStatus.ERROR, IOSCore.PLUGIN_ID, "Error while updating XCode project file", e));
		}
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, IOSCore.PLUGIN_ID, "Error while saving updated XCode project file", e));
		}
		//let it copy
		super.install();
	}	

}
