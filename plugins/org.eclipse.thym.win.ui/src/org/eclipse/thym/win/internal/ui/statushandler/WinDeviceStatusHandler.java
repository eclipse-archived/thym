/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Red Hat Inc. - initial API and implementation and/or initial documentation
 *		Zend Technologies Ltd. - initial implementation
 *		IBM Corporation - initial API and implementation
 *******************************************************************************/  

package org.eclipse.thym.win.internal.ui.statushandler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.thym.ui.status.AbstractStatusHandler;
import org.eclipse.thym.win.core.build.WinConstants;
import org.eclipse.thym.win.internal.ui.Messages;

public class WinDeviceStatusHandler extends AbstractStatusHandler {

	private class MissingDeviceDialog extends Dialog {

		public MissingDeviceDialog(Shell parentShell) {
			super(parentShell);
		}

		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			GridLayout layout = (GridLayout) composite.getLayout();
			layout.numColumns = 1;
			Link desc = new Link(composite, SWT.NONE);
			desc.setText(Messages.WinDevicesStatusHandler_Message);
			desc.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Program.launch(WinConstants.SDK_DOWNLOAD_URL);
				}
			});
			getShell().setText(Messages.WinDevicesStatusHandler_Title);
			return composite;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID,
					IDialogConstants.OK_LABEL, true);
		}

	}

	@Override
	public void handle(IStatus status) {
		Dialog dialog = new MissingDeviceDialog(getShell());
		dialog.open();
	}

	@Override
	public void handle(CoreException e) {
		handle(e.getStatus());
	}

}
