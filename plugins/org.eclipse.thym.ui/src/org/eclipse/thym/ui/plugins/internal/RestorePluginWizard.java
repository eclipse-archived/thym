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
package org.eclipse.thym.ui.plugins.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.plugin.RestorableCordovaPlugin;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.internal.status.StatusManager;

public class RestorePluginWizard extends Wizard {
	
	private PluginRestorePage page;
	private HybridProject project;
	
	public RestorePluginWizard(HybridProject project) {
		this.project = project;
		setWindowTitle("Restore Cordova Plug-in");
		setDefaultPageImageDescriptor(HybridUI.getImageDescriptor(HybridUI.PLUGIN_ID, CordovaPluginWizard.IMAGE_WIZBAN));
	}
	@Override
	public void addPages() {
		page = new PluginRestorePage(project);
		addPage(page);
		
	}
	@Override
	public boolean performFinish() {
		RestorableCordovaPlugin[] restorables = page.getSelectedRestorables();
		try {
			getContainer().run(true, true, new PluginRestoreOperation(project, restorables));
		} catch (InvocationTargetException e) {
			if (e.getTargetException() != null) {
				if(e.getTargetException() instanceof CoreException ){
					StatusManager.handle((CoreException) e.getTargetException());
				}else{
				ErrorDialog.openError(getShell(), "Error restoring plug-ins", null, 
						new Status(IStatus.ERROR, HybridUI.PLUGIN_ID, "Errors occured during plug-in installation", e.getTargetException() ));
				return false;
				}
			}
			return false;
		} catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
		return true;
	}

}
