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

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.ui.wizard.project.EngineConfigurationPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

public class CordovaPlatformWizard extends Wizard implements IWorkbenchWizard {

	private HybridProject project;
	private EngineConfigurationPage enginePage;

	public CordovaPlatformWizard() {
		setWindowTitle("Cordova Platform Wizard");
		setNeedsProgressMonitor(true);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		if (selection != null && !selection.isEmpty()) {
			Iterator<?> selects = selection.iterator();
			while (selects.hasNext()) {
				Object obj = selects.next();
				if (obj instanceof IResource) {
					IResource res = (IResource) obj;
					IProject project = res.getProject();
					HybridProject hybrid = HybridProject.getHybridProject(project);
					if (hybrid != null) {
						this.project = hybrid;
					}
				}
			}
		}
	}

	@Override
	public boolean performFinish() {
		try {
			project.getEngineManager().updateEngines(enginePage.getSelectedEngines());
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(), "Hybrid Mobile Engine Update failed",
					"Unable to update the active engine for the project " + project.getProject().getName(),
					e.getStatus());
			return false;
		}
		return true;
	}

	@Override
	public void addPages() {
		enginePage = new EngineConfigurationPage(project);
		addPage(enginePage);
	}

}
