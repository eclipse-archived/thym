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
package org.eclipse.thym.core.plugin.actions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.platform.IPluginInstallationAction;

public class JSModuleAction implements IPluginInstallationAction {
	
	private final File source;
	private final File target;
	private final String moduleName;
	
	public JSModuleAction(File source, File target, String jsmoduleName ){
		this.target = target;
		this.source = source;
		this.moduleName = jsmoduleName;
	}

	@Override
	public void install() throws CoreException {
		try {
			String scriptContent = FileUtils.readFileToString(source, "UTF-8");
			String content = "cordova.define(\"" + moduleName + "\", function(require, exports, module) {" + scriptContent + "});\n";
			if(!target.getParentFile().isDirectory()){// create the target directory
				target.getParentFile().mkdirs();
			}
			FileUtils.copyInputStreamToFile(new ByteArrayInputStream(content.getBytes()), target);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Error while creating file " + target, e));
		}
	}

	@Override
	public void unInstall() throws CoreException {
		if(target.exists()){
			if(!target.delete()){
				throw new CoreException(new Status(IStatus.ERROR, HybridCore.PLUGIN_ID, "Unable to delete file " + target));
			}
		}
	}

	@Override
	public String[] filesToOverwrite() {
		if(target.exists()){
			return new String[]{target.toString()};
		}
		return new String[0];
	}

}
