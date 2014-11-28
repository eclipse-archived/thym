/*******************************************************************************
 * Copyright (c) 2014 Zend Technologies Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Zend Technologies Ltd. - [447351] synchronization between Overview and Source page
 *******************************************************************************/
package org.eclipse.thym.ui.config.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

/**
 * Detects changes in config.xml done in a structured text editor (like in the
 * WTP XML Editor or in the Source tab of the Cordova Configuration Editor) and
 * reloads the editable widget of the Cordova Configuraton Editor, so the form
 * editor tabs can update their content accordingly.
 * 
 * @author Kaloyan Raev
 */
public class ReconcilingStrategy implements IValidator {

	@Override
	public void cleanup(IReporter reporter) {
	}

	@Override
	public void validate(IValidationContext helper, IReporter reporter) {
		ConfigEditor editor = getConfigEditor(helper);
		if (editor == null)
			return;

		editor.getWidgetModel().reloadEditableWidget();
	}

	private ConfigEditor getConfigEditor(IValidationContext helper) {
		String[] path = helper.getURIs();
		if (path.length == 0)
			return null;

		IFile file = ResourcesPlugin.getWorkspace().getRoot()
				.getFile(new Path(path[0]));
		if (file == null)
			return null;

		IEditorPart editor = getEditor(new FileEditorInput(file));
		if (editor == null)
			return null;

		if (!(editor instanceof ConfigEditor))
			return null;

		return (ConfigEditor) editor;
	}

	private IEditorPart getEditor(final IEditorInput editorInput) {
		final IEditorPart editor[] = new IEditorPart[1];
		Display.getDefault().syncExec(new Runnable() {
			// needs UI thread to retrieve active page
			public void run() {
				IWorkbenchPage activePage = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				if (activePage != null) {
					IEditorReference[] refs = activePage.findEditors(
							editorInput, ConfigEditor.ID,
							IWorkbenchPage.MATCH_ID
									| IWorkbenchPage.MATCH_INPUT);
					if (refs.length > 0) {
						editor[0] = refs[0].getEditor(true);
					}
				}
			}
		});
		return editor[0];
	}

}
