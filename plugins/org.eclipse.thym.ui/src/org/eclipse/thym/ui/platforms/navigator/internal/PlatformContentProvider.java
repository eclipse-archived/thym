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
package org.eclipse.thym.ui.platforms.navigator.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.engine.HybridMobileEngine;
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.ui.HybridUI;

public class PlatformContentProvider implements ITreeContentProvider, IResourceChangeListener {

	private Viewer viewer;

	public PlatformContentProvider() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (!(parentElement instanceof IFolder)) {
			return new Object[0];
		}
		IFolder folder = (IFolder) parentElement;
		if (folder.getProjectRelativePath().segmentCount() > 1) {// only platforms folder at the root of the project
			return new Object[0];
		}
		List<HybridPlatformFolder> platformFolders = new ArrayList<>();
		try {
			HybridProject project = HybridProject.getHybridProject(folder.getProject());
			for (IResource member : folder.members()) {
				if (member instanceof IFolder) {
					IFolder platformFolder = (IFolder) member;
					platformFolders.add(
							new HybridPlatformFolder((IFolder) member, getPlatform(project, platformFolder.getName())));
				}
			}
			
		} catch (CoreException e) {
			HybridUI.log(IStatus.ERROR, "Error reading cordova platforms", e);
		}
		return platformFolders.toArray();
	}

	private HybridMobileEngine getPlatform(HybridProject project, String name) {
		HybridMobileEngine[] engines = project.getEngineManager().getEngines();
		for (HybridMobileEngine engine : engines) {
			if (engine.getName().equals(name)) {
				return engine;
			}
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (!(element instanceof IFolder)) {
			return false;
		}
		IFolder folder = (IFolder) element;
		if (folder.getProjectRelativePath().segmentCount() > 1) {// folder at the root of the project?
			return false;
		}
		try {
			return folder.members().length > 0;
		} catch (CoreException e) {
			HybridUI.log(IStatus.ERROR, "Error reading cordova platforms", e);
		}
		return false;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (viewer == null || delta == null)
			return;
		IResourceDelta[] deltas = delta.getAffectedChildren();
		for (int i = 0; i < deltas.length; i++) {
			if (deltas[i].findMember(new Path(PlatformConstants.DIR_PLATFORMS)) != null) {
				viewer.getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						viewer.refresh();
					}
				});
				return;
			}
		}
	}

}
