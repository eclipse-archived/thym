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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.ui.platforms.internal.PlatformRemovalAction;
import org.eclipse.thym.ui.platforms.navigator.internal.HybridPlatformFolder;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class RemovePlatformHandler extends AbstractCordovaHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = getSelection();
		if (selection.isEmpty()) {
			return null;
		}
		List<HybridMobileEngine> platformsToRemove = new ArrayList<>();
		HybridProject project = null;
		@SuppressWarnings("rawtypes")
		Iterator it = selection.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof HybridPlatformFolder) {
				HybridPlatformFolder folder = (HybridPlatformFolder) o;
				platformsToRemove.add(folder.getPlatform());
				project = HybridProject.getHybridProject(folder.getFolder().getProject());
			}
		}
		new PlatformRemovalAction(project, platformsToRemove).run();
		return null;
	}

	private IStructuredSelection getSelection() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			ISelection selection = window.getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection)
				return (IStructuredSelection) selection;
		}
		return StructuredSelection.EMPTY;
	}

}
