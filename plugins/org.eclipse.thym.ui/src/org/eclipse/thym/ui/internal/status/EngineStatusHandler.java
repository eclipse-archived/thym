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
package org.eclipse.thym.ui.internal.status;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.thym.core.HybridMobileStatus;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.internal.properties.EnginePropertyPage;
import org.eclipse.thym.ui.status.AbstractStatusHandler;
import org.eclipse.ui.dialogs.PreferencesUtil;
/**
 * Status handler for missing cordova engine cases. 
 * 
 */
public class EngineStatusHandler extends AbstractStatusHandler implements IStatusHandler {

	@Override
	public Object handleStatus(IStatus status, Object source)
			throws CoreException {
		HybridMobileStatus  hs = (HybridMobileStatus) status;
		
		final boolean open = MessageDialog.openQuestion(AbstractStatusHandler.getShell(), "Missing or incomplete Hybrid Mobile Engine", 
				NLS.bind("{0} \n\nWould you like to modify Hybrid Mobile Engine preferences to correct this issue?",hs.getMessage() ));
		
		if(open){
			PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(getShell(), hs.getProject(), 
					EnginePropertyPage.PAGE_ID, new String[]{EnginePropertyPage.PAGE_ID}, null);
			return (dialog != null && dialog.open() == Window.OK)? Boolean.TRUE: Boolean.FALSE; 
		}
		return Boolean.FALSE;
	}
	
	@Override
	public void handle(IStatus status) {
		try {
			handleStatus(status, null);
		} catch (CoreException e) {
			HybridUI.log(IStatus.ERROR, "Handle status failed", e);
		}
	}
	
	@Override
	public void handle(CoreException e) {
		handle(e.getStatus());
	}

}
