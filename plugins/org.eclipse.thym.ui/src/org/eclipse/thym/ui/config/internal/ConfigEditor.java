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
package org.eclipse.thym.ui.config.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.config.Widget;
import org.eclipse.thym.core.config.WidgetModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;

public class ConfigEditor extends FormEditor {

	private SourceEditor sourceEditor;
	private Widget widget;
	private WidgetModel model;
	
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		setTitle(input);
	}

	private void setTitle(IEditorInput input) {
		String title = null;
		Widget w = getWidget();
		if(w != null ){
			title = getWidget().getName();
		}
		if(title == null || title.isEmpty() ){
			IResource res = (IResource)input.getAdapter(IResource.class);
			if(res != null ){
				title = res.getProject().getName();
			}
		}
		setPartName(title);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		sourceEditor.doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		//Not supported
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	protected void addPages() {
		sourceEditor = new SourceEditor();
		try {
			addPage(new EssentialsPage(this));
			addPage(new PropertiesPage(this));
//			addPage(new IconsPage(this)); disabled until JBIDE-15746 is resolved.
			int sourcePageIndex = addPage(sourceEditor, getEditorInput());
			
			setPageText(sourcePageIndex, "config.xml");
			firePropertyChange(PROP_TITLE);
			IFile f = getFile();
			if (f != null && f.exists()) {
				new ResourceChangeListener(this, getContainer());
			}
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Widget getWidget() {
		if (widget == null) {
			IFile file = (IFile) getEditorInput().getAdapter(IFile.class);
			if (file != null) {
				HybridProject prj = HybridProject.getHybridProject(file
						.getProject());
				WidgetModel model = WidgetModel.getModel(prj);
				try {
					widget = model.getWidgetForEdit();
				} catch (CoreException e) {
					HybridCore.log(IStatus.ERROR, "Error when retrieving the widget model", e);
				}
			}
		}
		return widget;
	}
	
	public WidgetModel getWidgetModel() {
		if (model == null) {
			IFile file = (IFile) getEditorInput().getAdapter(IFile.class);
			if (file != null) {
				HybridProject prj = HybridProject.getHybridProject(file
						.getProject());
				model = WidgetModel.getModel(prj);
			}
		}
		return model;
	}
	
	private IFile getFile() {
		IEditorInput input = getEditorInput();
		return (input instanceof IFileEditorInput) ? ((IFileEditorInput) input)
				.getFile() : null;
	}
	
	class ResourceChangeListener implements IResourceChangeListener, IResourceDeltaVisitor {
		ConfigEditor editorPart;

		ResourceChangeListener(ConfigEditor editorPart, Composite container) {
			this.editorPart = editorPart;
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.addResourceChangeListener(this);
			container.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					workspace.removeResourceChangeListener(ResourceChangeListener.this);
				}
			});
		}
		
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			try {
				if (delta != null){
					delta.accept(this);
				}
			} catch (CoreException exception) {
				HybridCore.log(IStatus.ERROR, "Error when retrieving IResourceDelta", exception);
			}
		}

		public boolean visit(IResourceDelta delta) {
			if (delta == null
					|| !delta.getResource().equals(
							((FileEditorInput) getEditorInput()).getFile())){
				return true;
			}

			if (delta.getKind() == IResourceDelta.REMOVED) {
				if ((IResourceDelta.MOVED_TO & delta.getFlags()) == 0) {
					if (!isDirty())
						closeEditor();
				} else { 
					final IFile newFile = ResourcesPlugin.getWorkspace()
							.getRoot().getFile(delta.getMovedToPath());
					Display display = getSite().getShell().getDisplay();
					display.asyncExec(new Runnable() {
						public void run() {
							editorPart.setInput(new FileEditorInput(newFile));
						}
					});
				}
			}
			return false;
		}

		private void closeEditor() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					editorPart.getSite().getPage().closeEditor(editorPart, false);
				}
			});
		}
	}
}
