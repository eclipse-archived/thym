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
package org.eclipse.thym.ios.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.thym.ui.status.AbstractStatusHandler;

public class XCodeVersionErrorStatusHandler extends AbstractStatusHandler {

	private static final String TITLE = "iOS Build and Run";

	@Override
	public void handle(IStatus status) {
		MessageDialog.openError(AbstractStatusHandler.getShell(), TITLE, status.getMessage());
	}

	@Override
	public void handle(CoreException e) {
		handle(e.getStatus());
	}

}
