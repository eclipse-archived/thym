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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.thym.core.plugin.actions.CopyFileAction;
import org.eclipse.thym.ios.core.IOSCore;
import org.eclipse.thym.ios.core.pbxproject.PBXFile;
import org.eclipse.thym.ios.core.pbxproject.PBXProject;
import org.eclipse.thym.ios.core.pbxproject.PBXProjectException;

public class IOSSourceFileAction extends CopyFileAction {
	
	private final boolean isFramework;
	private final String compilerFlags;
	private final File pbxFile;
	private final String path;

	public IOSSourceFileAction(File source, File target, File pbxFile, String path, boolean framework, String compilerFlags) {
		super(source, target);
		this.compilerFlags = compilerFlags;
		this.isFramework = framework;
		this.pbxFile = pbxFile;
		this.path = path;
	}
	
	
	@Override
	public void install() throws CoreException {
		PBXProject project = new PBXProject(pbxFile);
		
		try {
			project.addSourceFile(getPBXFile());
			if(isFramework){
				 project.addFramework(getPBXFile());
				 //Library search paths are project relative 
				 //we remove the first segment which is the project
				 //folder itself.
				 PBXFile searchPath = getPBXFile();
				 String rawPath = searchPath.getPath();
				 IPath path = new Path(rawPath);
				 path = path.removeFirstSegments(1);
				 searchPath.setPath(path.toString());
	             project.addToLibrarySearchPaths(searchPath);
			}
			project.persist();
		} catch (PBXProjectException e) {
			throw new CoreException(new Status(IStatus.ERROR, IOSCore.PLUGIN_ID, "Error updating XCode project file", e));
		}
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, IOSCore.PLUGIN_ID, "Error while saving updated XCode project file", e));
		}
		//let it do the copy
		super.install();
	}


	private PBXFile getPBXFile() {
		PBXFile file = new PBXFile(path);
		file.setFramework(isFramework);
		if(compilerFlags != null && !compilerFlags.isEmpty()){
			file.setCompilerFlags(compilerFlags);
		}
		return file;
	}
	
	
	@Override
	public void unInstall() throws CoreException {
		// TODO Auto-generated method stub
		super.unInstall();
	}
	

}
