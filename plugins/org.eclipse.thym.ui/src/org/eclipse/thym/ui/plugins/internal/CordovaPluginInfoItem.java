/*******************************************************************************
 * Copyright (c) 2013, 2015 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.plugins.internal;

import java.util.Iterator;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.thym.core.plugin.registry.CordovaPluginRegistryMapper;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPluginInfo;

public class CordovaPluginInfoItem extends ControlListItem<CordovaRegistryPluginInfo>{

	private static final int MAX_DESCRIPTION_CHARS = 162;
	private final CordovaPluginCatalogViewer viewer;
	private final CordovaPluginWizardResources resources;
	private Button checkbox;
	private boolean installed;
	private Label nameLabel;
	private Label description;
	private String nameString;
	private String descriptionText;
	private Label oldIdLabel;

	public CordovaPluginInfoItem(Composite parent, CordovaRegistryPluginInfo element, CordovaPluginWizardResources resources, CordovaPluginCatalogViewer viewer, boolean installed) {
		super(parent,SWT.NULL, element);
		this.resources = resources;
		this.viewer = viewer;
		this.installed = installed;
	}

	@Override
	protected void refresh() {
		if(nameLabel == null ){//already created¸
			createContent();
		}
		checkbox.setEnabled(!installed);
		if(installed){
			nameLabel.setFont(resources.getSmallItalicHeaderFont());
			description.setFont(resources.getItalicFont());
		}
		nameLabel.setText(getNameString());
		description.setText(getDescriptionText()); 
		if(oldIdLabel != null ){
			oldIdLabel.setText(NLS.bind("formerly: {0}", CordovaPluginRegistryMapper.toOld(getData().getName())));
		}
	}
	



	private String getDescriptionText() {
		if(descriptionText == null ){
			descriptionText = getData().getDescription();
			if (descriptionText == null) {
				descriptionText = ""; //$NON-NLS-1$
			}
			if (descriptionText.length() > MAX_DESCRIPTION_CHARS) {
				descriptionText = descriptionText.substring(0, MAX_DESCRIPTION_CHARS);
			}
			descriptionText = descriptionText.replaceAll("(\\r\\n)|\\n|\\r", " ");//$NON-NLS-1$ //$NON-NLS-2$
		}
		return descriptionText;
	}

	private String getNameString() {
		if(nameString != null )
		{
			return nameString;
		}
		IStructuredSelection selection = viewer.getSelection();
		if (selection == null || selection.isEmpty()) {
			checkbox.setSelection(false);
		}else{
			@SuppressWarnings("rawtypes")
			Iterator iter = selection.iterator();
			while (iter.hasNext()) {
				CordovaRegistryPluginInfo sel = (CordovaRegistryPluginInfo) iter.next();
				if (sel==this.getData()) {
					checkbox.setSelection(true);
					break;
				}
			}
		}
		if(installed){
			nameString = NLS.bind("{0} (installed)", getData().getName());
		}else{
			nameString = getData().getName();
		}
		return nameString;
	}
	
	private void createContent(){

		GridLayout layout = new GridLayout(3, false);
		layout.marginLeft = 7;
		layout.marginTop = 2;
		setLayout(layout);

		final Composite checkboxContainer = new Composite(this, SWT.NULL);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.BEGINNING).span(1, 4).applyTo(checkboxContainer);
		GridLayoutFactory.fillDefaults().spacing(1, 1).numColumns(3).applyTo(checkboxContainer);

		checkbox = new Button(checkboxContainer, SWT.CHECK | SWT.INHERIT_FORCE);
		checkbox.setText(" "); //$NON-NLS-1$
		GridDataFactory.swtDefaults()
		.align(SWT.CENTER, SWT.CENTER)
		.applyTo(checkbox);
		checkbox.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				viewer.modifySelection(getData(), checkbox.getSelection());
			}
		});

		nameLabel = new Label(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(2,1).align(SWT.FILL, SWT.CENTER).applyTo(nameLabel);
		nameLabel.setFont(resources.getSmallHeaderFont());
		
		description = new Label(this, SWT.NULL | SWT.WRAP);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 2).hint(100, SWT.DEFAULT).applyTo(description);
		
		String oldId = CordovaPluginRegistryMapper.toOld(getData().getName());
		if( oldId != null ){
			oldIdLabel = new Label(this, SWT.NULL);
			GridDataFactory.fillDefaults().grab(true, false).span(2,1).align(SWT.FILL, SWT.CENTER).applyTo(oldIdLabel);
			oldIdLabel.setFont(resources.getSubTextFont());
		}
		
		Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.fillDefaults()
		.indent(0, 2)
		.grab(true, false)
		.span(2, 1)
		.align(SWT.FILL, SWT.BEGINNING)
		.applyTo(separator);
	}
}
