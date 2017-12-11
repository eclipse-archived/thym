/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.internal.project;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class AbstractCordovaHandler extends AbstractHandler {

	protected IProject getProject(ExecutionEvent event) {
		final ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		if (!currentSelection.isEmpty() && currentSelection instanceof IStructuredSelection) {
			final Object object = ((IStructuredSelection) currentSelection).getFirstElement();
			return (IProject) Platform.getAdapterManager().getAdapter(object, IProject.class);
		}
		return null;
	}

}
