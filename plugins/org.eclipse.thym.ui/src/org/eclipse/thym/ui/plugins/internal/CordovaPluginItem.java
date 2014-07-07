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

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPlugin;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPluginVersion;

@SuppressWarnings("restriction")
public class CordovaPluginItem extends BaseCordovaPluginItem<CordovaRegistryPlugin> {
	
	private final static int MAX_DESCRIPTION_LENGTH = 162;
	
	private Label description;
	private Label nameLabel;
	private Label licenseLbl;
	private final CordovaPluginViewer viewer;
	private CordovaRegistryPluginVersion currentSelectedVersion;
	private ComboViewer versionComboViewer;

	public CordovaPluginItem(Composite parent, int style, CordovaRegistryPlugin element, CordovaPluginWizardResources resources, CordovaPluginViewer viewer ) {
		super(parent, element, resources);
		this.viewer = viewer;
		createContent();
	}

	@Override
	protected void refresh() {
	//Nothing to do
	}

	private void createContent(){
		GridLayout layout = new GridLayout(3, false);
		layout.marginLeft = 7;
		layout.marginTop = 2;
		layout.marginBottom = 2;
		setLayout(layout);

		Composite versionContainer = new Composite(this, SWT.INHERIT_NONE);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.BEGINNING).span(1, 2).applyTo(versionContainer);
		GridLayoutFactory.fillDefaults().spacing(1, 1).numColumns(2).applyTo(versionContainer);
		
		Combo versionCombo = new Combo(versionContainer, SWT.READ_ONLY);
		versionComboViewer = new ComboViewer(versionCombo);
		versionComboViewer.setContentProvider(new IStructuredContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			@Override
			public void dispose() {
			}
			@Override
			public Object[] getElements(Object inputElement) {
				List<CordovaRegistryPluginVersion> versions = getData().getVersions();
				return versions.toArray();
			}
		});
		versionComboViewer.setLabelProvider(new LabelProvider(){
			@Override
			public String getText(Object element) {
				CordovaRegistryPluginVersion ver = (CordovaRegistryPluginVersion)element;
				return ver.getVersionNumber();
			}
		});
		versionComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				CordovaRegistryPluginVersion selectedVersion = (CordovaRegistryPluginVersion) selection.getFirstElement();
				modifyVersionSelection(selectedVersion);
				
			}
		});
		versionComboViewer.setInput(getData().getVersions());
		versionComboViewer.setSelection(new StructuredSelection(getLatestCordovaRegistryPluginVersion()));
		
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(versionCombo);
		modifyVersionSelection(null);
		
		nameLabel = new Label(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(nameLabel);
		nameLabel.setFont(resources.getSmallHeaderFont());
		nameLabel.setText(getData().getName());
		
		description = new Label(this, SWT.NULL | SWT.WRAP);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).hint(100, SWT.DEFAULT).applyTo(description);
		
		final Composite detailsContainer = new Composite(this, SWT.INHERIT_NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER ).span(3, 1).applyTo(detailsContainer);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(detailsContainer);
		licenseLbl = new Label(detailsContainer, SWT.NONE);
		licenseLbl.setFont(resources.getSubTextFont());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(licenseLbl);
		updateValues();
	}

	private void modifyVersionSelection(CordovaRegistryPluginVersion selectedVersion) {
		if(selectedVersion == null ){
			selectedVersion = getLatestCordovaRegistryPluginVersion();
		}
		if(currentSelectedVersion != null){//remove the old version
			this.viewer.modifySelection(currentSelectedVersion, true);
		}
		currentSelectedVersion = selectedVersion;
		this.viewer.modifySelection(currentSelectedVersion, false);//now add the new one
	}
	
	private CordovaRegistryPluginVersion getLatestCordovaRegistryPluginVersion(){
		String latest = getData().getLatestVersion();
		List<CordovaRegistryPluginVersion> versions = getData().getVersions();
		for ( CordovaRegistryPluginVersion cordovaPluginVersion : versions) {
			if(cordovaPluginVersion.getVersionNumber().equals(latest)){
				return cordovaPluginVersion;
			}
		}
		return null;
	}

	private void updateValues() {
		IStructuredSelection sel = (IStructuredSelection) versionComboViewer.getSelection();
		if(sel.isEmpty())
			return;
		CordovaRegistryPluginVersion ver = (CordovaRegistryPluginVersion) sel.getFirstElement();
		setDescriptionText(ver.getDescription());
		licenseLbl.setText("License:"+ver.getLicense());
	}

	private void setDescriptionText(String descriptionText) {
		if (descriptionText == null) {
			descriptionText = ""; //$NON-NLS-1$
		}
		if (descriptionText.length() > MAX_DESCRIPTION_LENGTH) {
			descriptionText = descriptionText.substring(0, MAX_DESCRIPTION_LENGTH);
		}
		description.setText(descriptionText.replaceAll("(\\r\\n)|\\n|\\r", " ")); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
