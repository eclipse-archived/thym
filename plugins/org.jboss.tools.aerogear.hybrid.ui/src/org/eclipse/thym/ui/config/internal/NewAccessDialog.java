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
package org.eclipse.thym.ui.config.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.thym.core.config.Access;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.thym.ui.HybridUI;

public class NewAccessDialog extends Dialog {
	private Text txtOrigin;
	private Access access;
	private Widget widget;
	private WidgetModel model;
	private Button btnSubdomains;
	private Button btnBrowserOnly;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public NewAccessDialog(Shell parentShell, WidgetModel widgetModel) {
		super(parentShell);
		Assert.isNotNull(widgetModel);
		try {
			this.widget = widgetModel.getWidgetForEdit();
			this.model = widgetModel;
		} catch (CoreException e) {
			HybridUI.log(IStatus.WARNING, "Error retrieving Widget while on the NewAccessDialog", e);
		}
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("New Access");
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gl_container = new GridLayout(2, false);
		gl_container.marginTop = 10;
		container.setLayout(gl_container);
		
		Label lblOrigin = new Label(container, SWT.NONE);
		lblOrigin.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblOrigin.setText("Origin:");
		
		txtOrigin = new Text(container, SWT.BORDER);
		txtOrigin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		composite.setLayout(new RowLayout(SWT.VERTICAL));
		
		btnSubdomains = new Button(composite, SWT.CHECK);
		btnSubdomains.setText("Allow Subdomains");
		
		btnBrowserOnly = new Button(composite, SWT.CHECK);
		btnBrowserOnly.setText("Browser Only");

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		Point p = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		p.x = p.x+p.x/2;
		return p;
	}
	
	@Override
	protected void okPressed() {
		String origin = txtOrigin.getText();
		
		
		access = model.createAccess(widget);
		if(origin != null && !origin.isEmpty()){
			access.setOrigin(origin);
		}
		if(btnSubdomains.getSelection()){ // Set only if selected as defaults are false
			access.setSubdomains(true);
		}
		if(btnBrowserOnly.getSelection()){
			access.setBrowserOnly(true);
		}
		
		super.okPressed();
	}
	
	public Access getAccess(){
		return access;
	}
	
	
}
