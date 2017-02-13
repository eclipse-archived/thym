/*******************************************************************************
 * Copyright (c) 2013, 2016 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.android.core.adt;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.thym.android.core.AndroidCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.internal.cordova.CordovaCLI;
import org.eclipse.thym.core.internal.cordova.ErrorDetectingCLIResult;
import org.eclipse.thym.core.platform.AbstractNativeBinaryBuildDelegate;
/**
 * Build delegate for Android
 * @author Gorkem Ercan
 *
 */
public class BuildDelegate extends AbstractNativeBinaryBuildDelegate {

	private static String[] outputFolders = {"android","ant-build", "bin", "build", "outputs","apk"};

	@Override
	public void buildNow(IProgressMonitor monitor) throws CoreException {
		if(monitor.isCanceled())
			return;
		
		SubMonitor sm = SubMonitor.convert(monitor, "Build project for Android", 100);

		try {
			HybridProject hybridProject = HybridProject.getHybridProject(this.getProject());
			if (hybridProject == null) {
				throw new CoreException(new Status(IStatus.ERROR, AndroidCore.PLUGIN_ID,
						"Not a hybrid mobile project, can not generate files"));
			}
			String buildType = "--debug";
			if(isRelease()){
				buildType = "--release";
			}	
			IStatus status = CordovaCLI.newCLIforProject(hybridProject).build(sm.newChild(70),"android",buildType).convertTo(ErrorDetectingCLIResult.class).asStatus();
			this.getProject().refreshLocal(IResource.DEPTH_INFINITE, sm.newChild(20));
			if(status.getSeverity() == IStatus.ERROR){
				throw new CoreException(status);
			}
			IFolder androidProject = hybridProject.getProject().getFolder("platforms/android");
			androidProject.accept(new IResourceProxyVisitor() {
				
				@Override
				public boolean visit(IResourceProxy proxy) throws CoreException {
					switch (proxy.getType()) {
					case IResource.FOLDER:
						for (String folder : outputFolders) {
 							if(folder.equals(proxy.getName())){
								return true;
							}
						}
						break;
					case IResource.FILE:
						if(isRelease() && proxy.getName().endsWith("-release-unsigned.apk")){
							setBuildArtifact(proxy.requestResource().getLocation().toFile());
							return false;
						}
						if(proxy.getName().endsWith("-debug.apk")){
							setBuildArtifact(proxy.requestResource().getLocation().toFile());
							return false;
						}
					default:
						break;
					}
					return false;
				}
			}, IContainer.INCLUDE_HIDDEN | IContainer.INCLUDE_PHANTOMS | IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS );
			
        	if(getBuildArtifact() == null || !getBuildArtifact().exists()){
        		throw new CoreException(new Status(IStatus.ERROR, AndroidCore.PLUGIN_ID, "Build failed... Build artifact does not exist"));
        	}
		}
		finally{
			sm.done();
		}
	}
}
