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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NewNameValueDialog extends Dialog {
	private Text txtName;
	private Text txtValue;
	private String name;
	private String value;
	private String title;


	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public NewNameValueDialog(Shell parentShell, String title ) {
		super(parentShell);
		this.setShellStyle(SWT.DIALOG_TRIM);
		this.title = title;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		if(title != null )
			getShell().setText(title);

		Composite contents = new Composite(parent, SWT.NONE);
		contents.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(2, false);
		layout.marginTop = 10;
		contents.setLayout(layout);

		Label lblName = new Label(contents, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblName.setText("Name:");

		txtName = new Text(contents, SWT.BORDER);
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		Label lblValue = new Label(contents, SWT.NONE);
		lblValue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblValue.setText("Value:");

		txtValue = new Text(contents, SWT.BORDER);
		txtValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		return contents;
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
		name = txtName.getText();
		value = txtValue.getText();
		
		if(name == null || name.isEmpty()){
			return;
		}
		super.okPressed();
	}

	public String getValue() {
		return value;
	}
	
	public String getName(){
		return name;
	}
}
