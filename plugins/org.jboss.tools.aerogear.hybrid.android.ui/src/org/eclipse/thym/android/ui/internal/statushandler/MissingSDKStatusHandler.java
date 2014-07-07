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
package org.eclipse.thym.android.ui.internal.statushandler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.thym.android.ui.internal.preferences.AndroidPreferencePage;
import org.eclipse.thym.ui.status.AbstractStatusHandler;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class MissingSDKStatusHandler extends AbstractStatusHandler {

	@Override
	public void handle(IStatus status) {
		boolean define = MessageDialog.openQuestion(AbstractStatusHandler.getShell(), "Missing Android SDK",
				"Location of the Android SDK must be defined. Define Now?");
		if(define){
			PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), AndroidPreferencePage.PAGE_ID, 
					null, null);
			dialog.open();
		}
	}

	@Override
	public void handle(CoreException e) {
		handle(e.getStatus());
	}
}
