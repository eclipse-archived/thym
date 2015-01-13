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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.ui.util.DirectorySelectionGroup;
import org.eclipse.thym.ui.util.HybridProjectContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public abstract class BaseExportWizardDestinationPage extends WizardPage{

	
	private CheckboxTableSelectionGroup projectGroup;
	private CheckboxTableSelectionGroup platformsGroup;
	private DirectorySelectionGroup destinationDirectoryGroup;
	private IStructuredSelection initialSelection;
	
	protected BaseExportWizardDestinationPage(String pageName, IStructuredSelection selection) {
		super(pageName);
		this.initialSelection = selection;
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(1, false));
		
		projectGroup = new CheckboxTableSelectionGroup(container, SWT.NONE);
		projectGroup.setText("Select Projects:");
		projectGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		projectGroup.getTableViewer().setContentProvider(new HybridProjectContentProvider());
		projectGroup.getTableViewer().setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		projectGroup.getTableViewer().setInput(HybridCore.getHybridProjects());
		projectGroup.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setPageComplete(validatePage());
			}
		});
		
		platformsGroup = new CheckboxTableSelectionGroup(container, SWT.NONE);
		platformsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		platformsGroup.setText("Select Platforms:");
		platformsGroup.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setPageComplete(validatePage());
			}
		});
		platformsGroup.getTableViewer().setContentProvider(getPlatformContentProvider());
		platformsGroup.getTableViewer().setLabelProvider(getPlatformLabelProvider());
		
		destinationDirectoryGroup = new DirectorySelectionGroup(container, SWT.NONE);
		destinationDirectoryGroup.setText("Destination:");
		destinationDirectoryGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		destinationDirectoryGroup.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setPageComplete(validatePage());
			}
		});

		restoreWidgetValues();
		setupFromInitialSelection();
		setPageComplete(validatePage());
		
	}
	
	protected abstract IContentProvider getPlatformContentProvider();
	protected abstract IBaseLabelProvider getPlatformLabelProvider();
	
	private void setupFromInitialSelection() {
		if(initialSelection != null && !initialSelection.isEmpty()){
			Iterator<?> selects = initialSelection.iterator();
			while (selects.hasNext()) {
				Object obj  = selects.next();
				if(obj instanceof IResource ){
					IResource res = (IResource)obj;
					IProject project = res.getProject();
					HybridProject hybrid = HybridProject.getHybridProject(project);
					if(hybrid != null ){
						projectGroup.getTableViewer().setChecked(hybrid, true);
					}
				}
			}
		}
	}

	protected CheckboxTableSelectionGroup getPlatformsGroup(){
		return this.platformsGroup;
	}
	
	protected boolean validatePage(){
		TableItem[] items = projectGroup.getTableViewer().getTable().getItems();
		if(items== null || items.length <1 ){
			setMessage("No suitable projects are available", ERROR);
			return false;
		}
		Object[] selectedProjects = projectGroup.getTableViewer().getCheckedElements();
		if(selectedProjects.length <1 ){
			setMessage("No projects are selected. Please select projects to export" ,ERROR);
			return false;
		}
		Object[] selection = platformsGroup.getTableViewer().getCheckedElements();
		if(selection.length < 1){
			setMessage("No platform is selected. Please select a platform", ERROR);
			return false;
		}
		String destination = destinationDirectoryGroup.getValue();
		if(destination == null || destination.isEmpty()){
			setMessage("Specify a destination directory", ERROR);
			return false;
		}
		File dstFile = new File(destination);
		if(!DirectorySelectionGroup.isValidDirectory(dstFile)){
			setMessage("Specified destination is not a valid directory",ERROR );
			return false;
		}
		setMessage(null, NONE);
		return true;	
	}
	
	void saveWidgetValues(){
		IDialogSettings settings = getDialogSettings();
		if(settings != null ){
			destinationDirectoryGroup.saveHistory(settings);
		}
	}
	
	private void restoreWidgetValues(){
		IDialogSettings settings = getDialogSettings();
		if(settings != null ){
			destinationDirectoryGroup.restoreHistory(settings);
		}
	}
	
	public String getDestinationDirectory(){
		return destinationDirectoryGroup.getValue();
	}
	
	public List<HybridProject> getSelectedProjects(){
		Object[] checked = projectGroup.getTableViewer().getCheckedElements();
		ArrayList<HybridProject> list = new ArrayList<HybridProject>(checked.length);
		for (int i = 0; i < checked.length; i++) {
			list.add((HybridProject)checked[i]);
		}
		return list;
	}
	
}
