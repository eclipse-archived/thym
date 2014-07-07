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
package org.eclipse.thym.ui.internal.wizard.imports;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class HybridMobileProjectImportWizard extends Wizard implements
		IImportWizard {
	
	private static final String HYBRID_MOBILE_IMPORT_SECTION = "HybridMobileProjectImportWizard";
	private static final String IMAGE_WIZBAN = "/icons/wizban/newcordovaprj_wiz.png";
	private IStructuredSelection currentSelection;
	private HybridProjectImportPage page;
	
	public HybridMobileProjectImportWizard() {
		super();
        setNeedsProgressMonitor(true);
        IDialogSettings wizardSettings = getImportWizardDialogSettings();
		setDialogSettings(wizardSettings);      
	}


	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle("Import");
		setDefaultPageImageDescriptor(HybridUI.getImageDescriptor(HybridUI.PLUGIN_ID, IMAGE_WIZBAN));
        this.currentSelection = selection;	
	}

	@Override
	public boolean performFinish() {
		return page.createProjects();
	}
	
	@Override
	public void addPages() {
		super.addPages();
		page = new HybridProjectImportPage();
		this.addPage(page);
	}

	private IDialogSettings getImportWizardDialogSettings() {
		IDialogSettings workbenchSettings = HybridUI.getDefault().getDialogSettings();
		
		IDialogSettings wizardSettings = workbenchSettings
				.getSection(HYBRID_MOBILE_IMPORT_SECTION);
		if (wizardSettings == null) {
			wizardSettings = workbenchSettings
					.addNewSection(HYBRID_MOBILE_IMPORT_SECTION);
		}
		return wizardSettings;
	}
	
}
