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
package org.eclipse.thym.ui.plugins.navigator.internal;

import java.util.List;

import org.eclipse.core.resources.IFolder;
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
import org.eclipse.thym.core.platform.PlatformConstants;
import org.eclipse.thym.core.plugin.CordovaPlugin;
import org.eclipse.thym.ui.HybridUI;

public class PluginContentProvider implements ITreeContentProvider, IResourceChangeListener {
	
	private Viewer viewer;
	
	public PluginContentProvider() {
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
		if(!(parentElement instanceof IFolder)){
			return new Object[0];
		}
		IFolder folder = (IFolder) parentElement;
		if(folder.getProjectRelativePath().segmentCount()>1){//only plugins folder at the root of the project
			return new Object[0];
		}
		HybridProject project = HybridProject.getHybridProject(folder.getProject());
		try {
			List<CordovaPlugin> plugins = project.getPluginManager().getInstalledPlugins();
			return plugins.toArray();
		}catch(CoreException e){
			HybridUI.log(IStatus.ERROR, "Error retrieving the installed plugins", e);	
		}

		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if(!(element instanceof IFolder)){
			return false;
		}
		IFolder folder = (IFolder) element;
		if(folder.getProjectRelativePath().segmentCount()>1){//folder at the root of the project?
			return false;
		}
		HybridProject project = HybridProject.getHybridProject(folder.getProject());
		try {
			List<CordovaPlugin> plugins = project.getPluginManager().getInstalledPlugins();
			return plugins.isEmpty();
		} catch (CoreException e) {
			HybridUI.log(IStatus.ERROR, "Error determining the installed plugins", e);
		}
		return false;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if(viewer == null || delta == null )
			return;
		IResourceDelta[] deltas = delta.getAffectedChildren();
		for (int i = 0; i < deltas.length; i++) {
			if(deltas[i].findMember(new Path(PlatformConstants.DIR_PLUGINS)) != null){
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
