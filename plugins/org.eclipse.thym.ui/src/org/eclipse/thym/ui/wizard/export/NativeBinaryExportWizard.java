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
package org.eclipse.thym.ui.wizard.export;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.extensions.NativeProjectBuilder;
import org.eclipse.thym.core.platform.AbstractNativeBinaryBuildDelegate;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.internal.status.StatusManager;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

public class NativeBinaryExportWizard extends Wizard implements IExportWizard {

	private static final String WIZARD_TITLE = "Export Mobile Application";
	private static final String DIALOG_SETTINGS_KEY = "NativeBinaryExportWizard";
	private NativeBinaryDestinationPage pageOne;
	private IStructuredSelection initialSelection;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(WIZARD_TITLE);
		setNeedsProgressMonitor(true);
		this.initialSelection=selection;
		IDialogSettings workbenchSettings= HybridUI.getDefault().getDialogSettings();
		IDialogSettings section= workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
		setDialogSettings(section);
	}

	@Override
	public boolean performFinish() {
		List<HybridProject> projects =  pageOne.getSelectedProjects();
		List<NativeProjectBuilder> builders = pageOne.getSelectedPlatforms();
		ArrayList<AbstractNativeBinaryBuildDelegate> delegates = new ArrayList<AbstractNativeBinaryBuildDelegate>();
		for (HybridProject hybridProject : projects) {
			for (NativeProjectBuilder nativeProjectBuilder : builders) {
				try {
					AbstractNativeBinaryBuildDelegate dlg =nativeProjectBuilder.createDelegate(hybridProject.getProject(), null);
					delegates.add(dlg);
				} catch (CoreException e) {
					HybridCore.log(IStatus.ERROR, "Error creating native binary builder delegate for " +nativeProjectBuilder.getPlatform(), e);
				}
			}
		}
		
		NativeBinaryExportOperation op = new NativeBinaryExportOperation(delegates,new File(pageOne.getDestinationDirectory()), pageOne);
		
		
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() != null) {
				if(e.getTargetException() instanceof CoreException ){
					StatusManager.handle((CoreException) e.getTargetException());
				}else{
					ErrorDialog.openError(getShell(), "Error exporting mobile application",null, 
							new Status(IStatus.ERROR, HybridUI.PLUGIN_ID, "Error while exporting mobile application", e.getTargetException() ));
					return false;
				}
			}
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		savePageSettings();
		return true;
	}

	@Override
	public void addPages() {
		super.addPages();
		pageOne = new NativeBinaryDestinationPage(WIZARD_TITLE,initialSelection);
		addPage(pageOne);
	}
	private void savePageSettings() {
		IDialogSettings workbenchSettings= HybridUI.getDefault().getDialogSettings();
		IDialogSettings section= workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
		if(section == null ){
			section = workbenchSettings.addNewSection(DIALOG_SETTINGS_KEY);
		}
		setDialogSettings(section);
		pageOne.saveWidgetValues();
	}
	
}
