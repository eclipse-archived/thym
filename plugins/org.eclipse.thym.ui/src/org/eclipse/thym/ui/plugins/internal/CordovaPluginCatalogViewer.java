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
package org.eclipse.thym.ui.plugins.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.plugin.registry.CordovaRegistryPluginInfo;

public class CordovaPluginCatalogViewer extends Composite {

	private CheckboxTreeViewer pluginTreeViewer;
	private TreeViewerColumn installedColumn;
	private TreeViewerColumn nameColumn;
	private Text descriptionText;
	private Button installedButton;
	private Composite parent;
	private PluginsFilter pluginsFilter = new PluginsFilter();
	private CordovaPluginViewerComparator pluginsComparator = new CordovaPluginViewerComparator();

	private HybridProject project;
	
	private List<CordovaRegistryPluginInfo> pluginsToInstall = new ArrayList<CordovaRegistryPluginInfo>();

	public CordovaPluginCatalogViewer(Composite parent, int style) {
		super(parent, style);
	}

	public void createControl(Composite container) {
		parent = new Composite(container, SWT.NONE);
		parent.setLayout(new GridLayout(2,false));
		Text filterText = new Text(parent, SWT.NONE);
		filterText.setFocus();
		GridDataFactory.fillDefaults().grab(true, false).span(1, 1).applyTo(filterText);
		
		filterText.setMessage("filter plug-ins");
		filterText.addKeyListener(new KeyAdapter() {
			
			public void keyReleased(KeyEvent ke) {
				String filter = ((Text)ke.widget).getText();
				if(filter.trim().isEmpty()){
					pluginsFilter.setSearchString("");
				} else {
					pluginsFilter.setSearchString(filter);
				}
				pluginTreeViewer.refresh();
		      }
		});
		
		installedButton = new Button(parent, SWT.CHECK);
		GridDataFactory.fillDefaults().grab(false, false).span(1, 1).applyTo(installedButton);
		installedButton.setText("Show Installed");
		installedButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selected = ((Button)e.getSource()).getSelection();
				pluginsFilter.showInstalled(selected);
				pluginTreeViewer.refresh();
			}
		});
		
		Composite treeViewerComposite = new Composite(parent, SWT.NONE);
		pluginTreeViewer = new CheckboxTreeViewer(treeViewerComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		pluginTreeViewer.addFilter(pluginsFilter);
		pluginTreeViewer.setComparator(pluginsComparator);
		Tree tree = pluginTreeViewer.getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		pluginTreeViewer.setUseHashlookup(true);
		GridDataFactory.fillDefaults().grab(true, true).hint(100,200).span(2, 3).applyTo(treeViewerComposite);
		
		installedColumn = new TreeViewerColumn(pluginTreeViewer, SWT.NONE);
		installedColumn.getColumn().setText("Installed");

		
		nameColumn = new TreeViewerColumn(pluginTreeViewer, SWT.NONE);
		nameColumn.getColumn().setText("Plug-in name");

		CordovaPluginInfoContentProvider provider = new CordovaPluginInfoContentProvider();
		CordovaPluginInfoLabelProvider labelProvider = new CordovaPluginInfoLabelProvider();
		pluginTreeViewer.setLabelProvider(labelProvider);
		pluginTreeViewer.setContentProvider(provider);
		pluginTreeViewer.setCheckStateProvider(labelProvider);
		
		pluginTreeViewer.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				//disable checking/unchecking installed plugins
				if(isPluginInstalled((CordovaRegistryPluginInfo)event.getElement())){
					Object obj = event.getSource();
					if(obj instanceof CheckboxTreeViewer){
						((CheckboxTreeViewer)obj).setChecked(event.getElement(), true);
						
					}
				}
				if(event.getChecked()){
					pluginsToInstall.add((CordovaRegistryPluginInfo)event.getElement());
				} else {
					pluginsToInstall.remove((CordovaRegistryPluginInfo)event.getElement());
				}
				
			}
		});
		
		pluginTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if(event.getSelection() != null &&  ((IStructuredSelection)event.getSelection()).getFirstElement() != null){
					String desc = 
							((CordovaRegistryPluginInfo)((IStructuredSelection)event.getSelection()).getFirstElement()).getDescription();
					if(desc != null && !desc.isEmpty()){
						descriptionText.setText(desc.trim());
					} else {
						descriptionText.setText("Plug-in has no description");
					}
				} else {
					descriptionText.setText("Select plug-in to see description");
				}
			}
		});
		
		TreeColumnLayout columnLayout = new TreeColumnLayout();
		columnLayout.setColumnData(installedColumn.getColumn(), new ColumnPixelData(60));
		columnLayout.setColumnData(nameColumn.getColumn(), new ColumnWeightData(100, 150));
		treeViewerComposite.setLayout(columnLayout);
		
		Group descGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		descGroup.setText("Plug-in description");
		GridDataFactory.fillDefaults().span(2, 1).hint(100, 50).applyTo(descGroup);
		descGroup.setLayout(new GridLayout());
		descriptionText = new Text(descGroup, SWT.READ_ONLY | SWT.V_SCROLL | SWT.CENTER | SWT.WRAP);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(descriptionText);
		descriptionText.setText("Select plug-in to see description");

	}

	public List<CordovaRegistryPluginInfo> getPluginsToInstall() {
		return pluginsToInstall;
	}

	public void setProject(HybridProject project) {
		this.project = project;
	}

	public void setInput(Object[] input) {
		pluginTreeViewer.setInput(input);
	}

	public Object getInput() {
		return pluginTreeViewer.getInput();
	}

	
	public Control getControl() {
		return parent;
	}
	
	public void addCheckStateListener(ICheckStateListener listener) {
		pluginTreeViewer.addCheckStateListener(listener);
	}
	
	private boolean isPluginInstalled(CordovaRegistryPluginInfo plugin){
		if (project != null) {
			if (project.getPluginManager().isPluginInstalled(plugin.getName())) {
				return true;
			}
		}
		return false;
	}
	
	private class CordovaPluginInfoContentProvider implements ITreeContentProvider {
		
		private Object[] pluginsInfo;
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.pluginsInfo = (Object[])newInput;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return pluginsInfo;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return null;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return false;
		}

	}
	
	private class CordovaPluginInfoLabelProvider extends LabelProvider implements ITableLabelProvider, 
		ICheckStateProvider{

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return null;
			case 1:
				if(element instanceof CordovaRegistryPluginInfo){
					return ((CordovaRegistryPluginInfo) element).getName();
				} else {
					return (String)element;
				}
			}
			return null;
		}

		@Override
		public boolean isChecked(Object element) {
			return isPluginInstalled((CordovaRegistryPluginInfo) element) || pluginsToInstall.contains(element);
		}

		@Override
		public boolean isGrayed(Object element) {
			if(pluginsFilter.isShowInstalled()){
				CordovaRegistryPluginInfo plugin = (CordovaRegistryPluginInfo) element;
				return isPluginInstalled(plugin);
			}
			return false;
		}
		
	}
	
	class PluginsFilter extends ViewerFilter {
		
		private String searchString;
		private boolean showInstalled;
		
		public void setSearchString(String s){
			this.searchString = ".*" + s + ".*";
		}
		
		public void showInstalled(boolean installed){
			this.showInstalled = installed;
		}
		
		public boolean isShowInstalled(){
			return this.showInstalled;
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (searchString == null || searchString.length() == 0) {
				if(!showInstalled){
					CordovaRegistryPluginInfo plugin = (CordovaRegistryPluginInfo) element;
					return !isPluginInstalled(plugin);
				}
				return true;
			}
			try{
				Pattern pattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
				CordovaRegistryPluginInfo plugin = (CordovaRegistryPluginInfo) element;
				boolean matchesPattern = pattern.matcher(plugin.getName()).matches();
				if(!showInstalled){
					return matchesPattern && !isPluginInstalled(plugin);
				}
				return matchesPattern;
			} catch (PatternSyntaxException e) {
				return false;
			}
		}
		
	}
	
	class CordovaPluginViewerComparator extends ViewerComparator{
		private static final String CORDOVA_NAMESPACE = "org.apache.cordova";
		private static final int CATEGORY_INSTALLED = 0;
		private static final int CATEGORY_CORDOVA = 1;
		private static final int CATEGORY_OTHER = 2;
		public int category(Object element) {
			CordovaRegistryPluginInfo info = (CordovaRegistryPluginInfo)element;
			if(pluginsFilter.isShowInstalled()){
				if(isPluginInstalled(info)){
					return CATEGORY_INSTALLED;
				}
			}
			//prioritize apache cordova plugins
			if(info.getName().startsWith(CORDOVA_NAMESPACE)){
				return CATEGORY_CORDOVA;
			}
			return CATEGORY_OTHER;
		}
	}
	
	
	

}
