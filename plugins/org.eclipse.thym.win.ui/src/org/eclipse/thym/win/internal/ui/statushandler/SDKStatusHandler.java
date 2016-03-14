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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.thym.ui.status.AbstractStatusHandler;
import org.eclipse.thym.win.internal.ui.Messages;
import org.eclipse.thym.win.internal.ui.preferences.WinPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Status handler for missing Windows Phone SDK.
 * 
 * @author Wojciech Galanciak, 2014
 *
 */
public class SDKStatusHandler extends AbstractStatusHandler {

	@Override
	public void handle(IStatus status) {
		boolean define = MessageDialog
				.openQuestion(AbstractStatusHandler.getShell(),
						Messages.SDKStatusHandler_Title,
						Messages.SDKStatusHandler_Message);
		if (define) {
			PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
					getShell(), WinPreferencePage.PAGE_ID, null, null);
			dialog.open();
		}
	}

	@Override
	public void handle(CoreException e) {
		handle(e.getStatus());
	}

}
