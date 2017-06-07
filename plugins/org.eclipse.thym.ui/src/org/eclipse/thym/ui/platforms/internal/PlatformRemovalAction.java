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
package org.eclipse.thym.ui.platforms.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.ui.PlatformUI;

public class PlatformRemovalAction extends Action {

	private List<HybridMobileEngine> platformsToRemove;
	private HybridProject project;
	private static final String UNINSTALL_ICON = "/icons/obj16/cordova_16.png";

	public PlatformRemovalAction(HybridProject project, List<HybridMobileEngine> platformsToRemove) {
		super("Remove Cordova Platform");
		this.project = project;
		this.platformsToRemove = platformsToRemove;
	}

	@Override
	public void run() {
		if (platformsToRemove != null && !platformsToRemove.isEmpty()) {
			try {
				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							for (HybridMobileEngine cordovaPlatform : platformsToRemove) {
								monitor.subTask(NLS.bind("Uninstalling {0} platform", cordovaPlatform.getName()));
								project.getEngineManager().removeEngine(cordovaPlatform, monitor, true);
							}
						} catch (CoreException e) {
							throw new InvocationTargetException(e);
						}

					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				HybridUI.log(IStatus.ERROR, "Error while removing a Cordova platform ", e);
			}
		}
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return HybridUI.getImageDescriptor(HybridUI.PLUGIN_ID, UNINSTALL_ICON);
	}

}
