/*******************************************************************************
 * Copyright (c) 2013, 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.internal.properties;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.plugin.CordovaPlugin;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.internal.engine.AvailableCordovaEnginesSection;
import org.eclipse.ui.dialogs.PropertyPage;

public class EnginePropertyPage extends PropertyPage {
	
	public static final String PAGE_ID = "org.eclipse.thym.ui.internal.properties.enginePropertyPage";
	
	private AvailableCordovaEnginesSection engineSection;
	
	
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(control);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(control);
		
		engineSection = new AvailableCordovaEnginesSection();
		engineSection.createControl(control);
		
		engineSection.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setValid(isValid());
				getContainer().updateButtons();
			}
		});
		noDefaultAndApplyButton();
		HybridProject hybridProject = getProject();
		HybridMobileEngine[] activeEngines = hybridProject.getEngineManager().getEngines();
		if(activeEngines != null){
			engineSection.setSelection(new StructuredSelection(activeEngines));
		}
		else{
			setValid(isValid());
		}
		return control;
	}

	private HybridProject getProject() {
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		HybridProject hybridProject = HybridProject.getHybridProject(project);
		return hybridProject;
	}
	
	@Override
	public boolean isValid() {
		setMessage(null);
		setErrorMessage(null);
		IStructuredSelection sel = (IStructuredSelection) engineSection.getSelection();
		if(sel != null){
			for (Iterator<?> iterator = sel.iterator(); iterator.hasNext();) {
				HybridMobileEngine engine = (HybridMobileEngine) iterator.next();
				try {
					List<CordovaPlugin> installedPlugins = getProject().getPluginManager().getInstalledPlugins();
					for (CordovaPlugin cordovaPlugin : installedPlugins) {
						IStatus status = cordovaPlugin.isEngineCompatible(engine);
						if( !status.isOK())
						{
							setMessage(status.getMessage(), status.getSeverity());
							return status.getSeverity() != IStatus.ERROR;
						}
					}
				} catch (CoreException e) {
					HybridUI.log(IStatus.WARNING, "Error while checking engine and plug-in compatability ",  e);
				}
				
			}
		}
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@SuppressWarnings("unchecked")
	public boolean performOk() {
		IStructuredSelection selection = (IStructuredSelection)engineSection.getSelection();
		@SuppressWarnings("rawtypes")
		List list = selection.toList();
		try {
			getProject().getEngineManager().updateEngines((HybridMobileEngine[]) list.toArray(new HybridMobileEngine[list.size()]));
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(), 
					"Hybrid Mobile Engine Update failed", "Unable to update the active engine for the project "+ getProject().getProject().getName(), 
					e.getStatus());
			return false;
		}
		return true;
	}

}