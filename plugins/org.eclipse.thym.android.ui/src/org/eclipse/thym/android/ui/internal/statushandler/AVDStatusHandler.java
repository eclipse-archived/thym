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
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.thym.android.core.adt.AndroidSDKManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.thym.core.HybridMobileStatus;

public class AVDStatusHandler implements IStatusHandler{

	@Override
	public Object handleStatus(IStatus status, Object source)
			throws CoreException {
	HybridMobileStatus hs = (HybridMobileStatus) status;
		
		boolean open = MessageDialog.openQuestion(getShell(), "Android AVD problem ", 
				NLS.bind("{0} \n\nWould you like to run Android AVD manager to correct this issue?",hs.getMessage() ));
		
		if(open){
			AndroidSDKManager sdk = AndroidSDKManager.getManager();
			sdk.startAVDManager();
		}
		return Boolean.FALSE;

	}
	
	private Shell getShell(){
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		if (windows.length > 0) {
			return windows[0].getShell();
		}
		return null;
	}
	

}
