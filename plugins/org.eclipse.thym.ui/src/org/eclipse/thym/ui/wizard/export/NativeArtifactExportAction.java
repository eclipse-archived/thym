/*******************************************************************************
 * Copyright (c) 2013, 2016 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.wizard.export;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.thym.ui.config.internal.ConfigEditor;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;


public class NativeArtifactExportAction extends Action {
	
	
	private ConfigEditor editor;
	

	
	public NativeArtifactExportAction(ConfigEditor editor ) {
		super();
		this.editor = editor;
	}


	@Override
	public void run() {
		
		IWorkbench workbench = PlatformUI.getWorkbench();		
		IStructuredSelection selection = null;
		if(editor != null ){
			IFileEditorInput editorInput = (IFileEditorInput) editor.getEditorInput().getAdapter(IFileEditorInput.class);
			if(editorInput != null ){
				selection = new StructuredSelection(editorInput.getFile().getProject());
				
			}
		}
		
		Wizard wizard = null;
		NativeBinaryExportWizard wiz = new NativeBinaryExportWizard();
		wiz.init(workbench, selection);
		wizard = wiz;
		
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
		dialog.open();
	}
	
}
