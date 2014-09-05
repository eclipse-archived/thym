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

import org.eclipse.core.runtime.Assert;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.platform.AbstractPluginInstallationActionsFactory;
import org.eclipse.thym.core.platform.IPluginInstallationAction;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.core.plugin.actions.CopyFileAction;
import org.eclipse.thym.core.plugin.actions.CreateFileAction;
import org.eclipse.thym.core.plugin.actions.JSModuleAction;
import org.eclipse.thym.core.plugin.actions.XMLConfigFileAction;

public class IOSPluginInstallationActionsFactory extends
		AbstractPluginInstallationActionsFactory {


	@Override
	public IPluginInstallationAction getSourceFileAction(String src,
			String targetDir, String framework, String pluginId, String compilerFlags) {
		File source = new File(getPluginDirectory(), src);
		StringBuilder targetPath = calculateTargetPath("Plugins",targetDir, pluginId,
				source);
		File target = new File(getProjectDirectory(), targetPath.toString());
		boolean isFramework= Boolean.parseBoolean(framework);
		File pbx = getPbxprojectFile();
		return new IOSSourceFileAction(source, target, pbx, targetPath.toString(), isFramework, compilerFlags );
	}


	@Override
	public IPluginInstallationAction getResourceFileAction(String src) {
		File source = new File(getPluginDirectory(), src);
		String targetPath = calculateTargetPath("Resources", null, null, source).toString();
		File target = new File(getProjectDirectory(), targetPath);
		File pbx = getPbxprojectFile();

		return new IOSResourceFileAction(source, target, pbx, targetPath);
	}

	@Override
	public IPluginInstallationAction getHeaderFileAction(String src, String targetDir, String pluginId) {
		File source = new File(getPluginDirectory(), src);
		StringBuilder targetPath = calculateTargetPath("Plugins", targetDir, pluginId, source);
		File target = new File(getProjectDirectory(), targetPath.toString());
		File pbx = getPbxprojectFile();

		return new IOSHeaderFileAction(source, target, pbx, targetPath.toString());
		
	}

	@Override
	public IPluginInstallationAction getAssetAction(String src, String target) {
		File sourceFile = new File(getPluginDirectory(),src);
		File targetFile = new File(XCodeProjectUtils.getPlatformWWWDirectory(getProjectDirectory()),target);
		return new CopyFileAction(sourceFile, targetFile);
	}

	@Override
	public IPluginInstallationAction getConfigFileAction(String target,
			String parent, String value) {
		File configFile = new File(getProjectDirectory(),target);
		return new XMLConfigFileAction(configFile, parent, value);

	}

	@Override
	public IPluginInstallationAction getLibFileAction(String src, String arch) {
		throw new UnsupportedOperationException("Not implemented for iOS");
	}

	@Override
	public IPluginInstallationAction getFrameworkAction(String src, String weak,String pluginId, String custom, String type, String parent){
		File pbx = getPbxprojectFile();
		boolean isWeak = Boolean.parseBoolean(weak);
		return new IOSFrameworkAction(src, isWeak, pbx);
	}


	@Override
	public IPluginInstallationAction getJSModuleAction(String src,
			String pluginId, String jsModuleName) {
		File sourceFile = new File(getPluginDirectory(),src);
		File targetFile = new File(XCodeProjectUtils.getPlatformWWWDirectory(getProjectDirectory()),
				PlatformConstants.DIR_PLUGINS+File.separator+pluginId+File.separator+src);
		return new JSModuleAction(sourceFile, targetFile,jsModuleName);
	}

	@Override
	public IPluginInstallationAction getCreatePluginJSAction(String content) {
		File pluginJs = new File(XCodeProjectUtils.getPlatformWWWDirectory(getProjectDirectory()),PlatformConstants.FILE_JS_CORDOVA_PLUGIN);
		return new CreateFileAction(content, pluginJs);
	}

	private File getPbxprojectFile() {
		HybridProject project = HybridProject.getHybridProject(getProject());
		Assert.isNotNull(project, "Installation actions can not be created for non-hybrid project types");
		File pbx = new File(getProjectDirectory(), project.getBuildArtifactAppName()+".xcodeproj/project.pbxproj");
		return pbx;
	}
	
	private StringBuilder calculateTargetPath(String groupDir, String targetDir,
			String pluginId, File source) {
		StringBuilder targetPath = new StringBuilder();
		HybridProject prj = HybridProject.getHybridProject(getProject());
		targetPath.append(prj.getBuildArtifactAppName()).append(File.separator);
		if(groupDir != null){
			targetPath.append(groupDir).append(File.separator);
		}
		if(pluginId != null){
			targetPath.append(pluginId).append(File.separator);
		}
		if(targetDir != null){
			targetPath.append(targetDir).append(File.separator);
		}
		targetPath.append(source.getName());
		return targetPath;
	}
	
}
