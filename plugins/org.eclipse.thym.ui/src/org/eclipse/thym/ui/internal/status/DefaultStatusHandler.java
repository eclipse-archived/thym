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
package org.eclipse.thym.ui.internal.status;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.thym.ui.status.AbstractStatusHandler;
import org.eclipse.ui.statushandlers.StatusManager;

public class DefaultStatusHandler extends AbstractStatusHandler {

	@Override
	public void handle(IStatus status) {
		StatusManager platformStatusMgr = StatusManager.getManager();
		platformStatusMgr.handle(status,StatusManager.SHOW | StatusManager.LOG);
	}

	@Override
	public void handle(CoreException e) {
		StatusManager platformStatusMgr = StatusManager.getManager();
		platformStatusMgr.handle(e,e.getStatus().getPlugin());
	}

}
