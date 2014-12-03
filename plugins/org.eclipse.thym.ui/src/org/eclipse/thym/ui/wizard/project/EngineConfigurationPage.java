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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.HybridMobileEngineManager;
import org.eclipse.thym.ui.internal.engine.AvailableCordovaEnginesSection;

public class EngineConfigurationPage extends WizardPage {

	private AvailableCordovaEnginesSection engineSection;

	protected EngineConfigurationPage(String pageName) {
		super(pageName);
		setTitle("Select a Hybrid Mobile Engine");
		setDescription("Select a hybrid mobile engine that will be used for building the mobile application");
	}

	@Override
	public void createControl(Composite parent) {
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
		
		setControl(control);
		setDefaultEngine();
		setPageComplete(validatePage());
		Dialog.applyDialogFont(getControl());
	}
	
	private boolean validatePage(){
		IStructuredSelection selection =  (IStructuredSelection)engineSection.getSelection();
		if(selection.isEmpty() ){
			setErrorMessage("Please select a Hybrid Mobile Engine ");
			return false;
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
	}
	
	private void setDefaultEngine() {
		HybridMobileEngine defaultEngine = HybridMobileEngineManager.getDefaultEngine();
		if(defaultEngine != null ){
			engineSection.setSelection(new StructuredSelection(defaultEngine));
		}
	}
	
	public HybridMobileEngine getSelectedEngine(){
		IStructuredSelection selection = (IStructuredSelection) engineSection.getSelection();
		HybridMobileEngine engine = (HybridMobileEngine) selection.getFirstElement();
		return engine;
	}
	
}
