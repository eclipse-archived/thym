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
package org.eclipse.thym.ui.plugins.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.equinox.internal.p2.ui.discovery.util.FilteredViewer;
import org.eclipse.equinox.internal.p2.ui.discovery.util.PatternFilter;
import org.eclipse.equinox.internal.p2.ui.discovery.util.SelectionProviderAdapter;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPlugin;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPlugin.RegistryPluginVersion;


@SuppressWarnings("restriction")
public class CordovaPluginViewer extends FilteredViewer {
	private CordovaPluginWizardResources resources;
	
	private static class CordovaPluginContentProvider implements
	IStructuredContentProvider {
		private List<CordovaRegistryPlugin> items;

		@Override
		public void dispose() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.items = (List<CordovaRegistryPlugin>) newInput;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if(items == null || items.isEmpty())
				return new Object[0];
			return items.toArray();
		}
		
	}

	private final SelectionProviderAdapter selectionProvider;
	private List<RegistryPluginVersion> selectedItems = new ArrayList<RegistryPluginVersion>(); 
	public CordovaPluginViewer(){
		this.selectionProvider = new SelectionProviderAdapter();
	}
	
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionProvider.addSelectionChangedListener(listener);
	}
	
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionProvider.removeSelectionChangedListener(listener);
	}
	
	public IStructuredSelection getSelection() {
		return (IStructuredSelection) selectionProvider.getSelection();
	}
	
	void modifySelection ( RegistryPluginVersion element, boolean remove){
		if (remove) {
			selectedItems.remove(element);
		}else if( !selectedItems.contains(element) ){
			selectedItems.add(element);
		}
		selectionProvider.setSelection(new StructuredSelection(selectedItems));
	}
	
	@Override
	protected PatternFilter doCreateFilter() {
		return new CordovaPluginFilter();
	}
	
	@Override
	protected StructuredViewer doCreateViewer(Composite container) {
		resources = new CordovaPluginWizardResources(container.getDisplay());
		StructuredViewer viewer = new PluginControlListViewer(container, SWT.BORDER) {

			@Override
			protected ControlListItem<CordovaRegistryPlugin> doCreateItem(
					Composite parent, Object element) {
				return doCreateViewerItem(parent, element);
			}
		};
		viewer.setContentProvider(new CordovaPluginContentProvider());
		return viewer;
	}
	
	
	private ControlListItem<CordovaRegistryPlugin> doCreateViewerItem(
			Composite parent, Object element) {
		return new CordovaPluginItem(parent, SWT.NULL, (CordovaRegistryPlugin)element,resources,this);
	}

}
