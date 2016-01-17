/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.internal.cordova;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;
/**
 * A specialized dialog that is used to show instructions 
 * to install required software on a cheat sheet tray. 
 * 
 * @author Gorkem Ercan
 *
 */
public class MissingRequirementsDialog extends TrayDialog {
	
	private String message;

	protected MissingRequirementsDialog(Shell shell) {
		super(shell);
		this.setHelpAvailable(false);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Link messageLnk = new Link(composite,SWT.NONE);
		messageLnk.setText(this.message);
		messageLnk.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				OpenCheatSheetAction ch = new OpenCheatSheetAction("org.eclipse.thym.ui.requirements.cordova");
				ch.run();
			}
		});
		return composite;
	}
	
	/**
	 * Sets the message to be displayed
	 * @param message
	 * @throws IllegalArgumentException if message is null
	 */
	public void setMessage(final String message){
		if(message == null ){
			throw new IllegalArgumentException("Message can not be null");
		}
		this.message = message;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, 
				IDialogConstants.OK_LABEL,
				true);
	}
}
