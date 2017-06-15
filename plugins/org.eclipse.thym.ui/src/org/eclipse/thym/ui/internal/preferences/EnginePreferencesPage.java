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
package org.eclipse.thym.ui.internal.preferences;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.engine.internal.cordova.CordovaEngineProvider;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.internal.engine.AvailableCordovaEnginesSection;
import org.eclipse.thym.ui.internal.engine.AvailableCordovaEnginesSection.EngineListChangeListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class EnginePreferencesPage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private AvailableCordovaEnginesSection engineSection;
	
	public EnginePreferencesPage(){
		super();
		setDescription("Add or remove Hybrid Mobile Engines. By default, checked engine is used for the newly created Hybrid Mobile projects.");
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(control);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(control);
		
		engineSection = new AvailableCordovaEnginesSection();
		engineSection.createControl(control);
		
		engineSection.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setValid(validate());
			}
		});
		engineSection.addEngineListChangeListener(new EngineListChangeListener() {
			@Override
			public void listChanged() {
				initDefaultEngine();
			}
		});
		noDefaultAndApplyButton();
		initDefaultEngine();
		setValid(validate());
		return control;
	}

	private void initDefaultEngine() {
		List<HybridMobileEngine> defaultEngines = CordovaEngineProvider.getInstance().defaultEngines(); 
		if(defaultEngines != null ){
			engineSection.setSelection(new StructuredSelection(defaultEngines));
		}else{
			List<HybridMobileEngine> engines = engineSection.getListedEngines();
			if(engines != null && engines.size() ==1){
				engineSection.setSelection(new StructuredSelection(engines.get(0)));
			}
		}
	}
	
	private boolean validate() {
		if(engineSection.getSelection().isEmpty()){
			setErrorMessage("Default engine is not selected");
			return false;
		}
		setMessage(null);
		setErrorMessage(null);
		return true;
	}
	
	@Override
	public boolean performOk() {
		IStructuredSelection sel = (IStructuredSelection) engineSection.getSelection();
		if(sel.isEmpty()){
			getPreferenceStore().setToDefault(PlatformConstants.PREF_DEFAULT_ENGINE);
		}else{
			StringBuilder prefVal = new StringBuilder();
			Object[] selections = sel.toArray();
			for(int i=0; i< selections.length; i++){
				HybridMobileEngine engine =(HybridMobileEngine) selections[i];
				prefVal.append(engine.getName());
				prefVal.append(":");
				prefVal.append(engine.getSpec());
				prefVal.append(",");
			}
			getPreferenceStore().setValue(PlatformConstants.PREF_DEFAULT_ENGINE, prefVal.toString());
		}
		return true;
	}
	
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return HybridUI.getDefault().getPreferenceStore();
	}

}
