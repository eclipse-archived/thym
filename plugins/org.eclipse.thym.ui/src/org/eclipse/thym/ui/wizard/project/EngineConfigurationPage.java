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
package org.eclipse.thym.ui.wizard.project;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.internal.cordova.CordovaEngineProvider;
import org.eclipse.thym.ui.internal.engine.AvailableCordovaEnginesSection;
import org.eclipse.thym.ui.internal.engine.AvailableCordovaEnginesSection.EngineListChangeListener;

public class EngineConfigurationPage extends WizardPage {

	private AvailableCordovaEnginesSection engineSection;
	private HybridProject project;

	public EngineConfigurationPage() {
		this(null);
	}

	public EngineConfigurationPage(HybridProject project) {
		super("Configure Platforms");
		setTitle("Select a Hybrid Mobile Engine");
		setDescription("Select a hybrid mobile engine that will be used for building the mobile application");
		this.project = project;
	}

	@Override
	public void createControl(Composite parent) {
		((WizardDialog) this.getWizard().getContainer()).setMinimumPageSize(300, 400);
		Composite control = new Composite(parent, SWT.NONE);

		initializeDialogUnits(control);

		GridLayoutFactory.fillDefaults().applyTo(control);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(control);

		engineSection = new AvailableCordovaEnginesSection();
		engineSection.createControl(control);

		engineSection.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete(validatePage());

			}
		});
		engineSection.addEngineListChangeListener(new EngineListChangeListener() {

			@Override
			public void listChanged() {
				setPageComplete(validatePage());

			}
		});

		setControl(control);
		setEngines();
		setPageComplete(validatePage());
		Dialog.applyDialogFont(getControl());
	}

	private boolean validatePage() {
		return true;
	}

	private void setEngines() {
		if (project == null) {
			List<HybridMobileEngine> engines = CordovaEngineProvider.getInstance().defaultEngines();
			if (engines != null && engines.size() > 0) {
				engineSection.setSelection(new StructuredSelection(engines));
			}
		} else {
			HybridMobileEngine[] activeEngines = project.getEngineManager().getEngines();
			if(activeEngines != null){
				engineSection.setSelection(new StructuredSelection(activeEngines));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public HybridMobileEngine[] getSelectedEngines() {
		IStructuredSelection selection = (IStructuredSelection) engineSection.getSelection();
		@SuppressWarnings("rawtypes")
		List selected = selection.toList();
		return (HybridMobileEngine[]) selected.toArray(new HybridMobileEngine[selected.size()]);
	}

}
