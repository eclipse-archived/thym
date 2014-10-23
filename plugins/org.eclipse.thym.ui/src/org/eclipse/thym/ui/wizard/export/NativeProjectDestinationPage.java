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
package org.eclipse.thym.ui.wizard.export;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.extensions.PlatformSupport;
import org.eclipse.thym.ui.HybridUI;
import org.eclipse.thym.ui.internal.projectGenerator.ProjectGeneratorContentProvider;
import org.eclipse.thym.ui.internal.projectGenerator.ProjectGeneratorLabelProvider;
import org.eclipse.ui.dialogs.IOverwriteQuery;

public class NativeProjectDestinationPage extends BaseExportWizardDestinationPage implements IOverwriteQuery{

    private static final String IMAGE_WIZBAN = "/icons/wizban/exportnativeprj_wiz.png";

    protected NativeProjectDestinationPage(String pageName, IStructuredSelection initialSelection) {
        super(pageName, initialSelection);
        setImageDescriptor(HybridUI.getImageDescriptor(HybridUI.PLUGIN_ID, IMAGE_WIZBAN));
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        this.getPlatformsGroup().getTableViewer().setInput(HybridCore.getPlatformSupports());
    }
    @Override
    public String queryOverwrite(String pathString) {
        final MessageDialog dialog = new MessageDialog(getShell(),
                "Overwrite Files?",
                null,
                "Directory " + pathString+ " already exists. Would you like to overwrite it?",
                 MessageDialog.QUESTION,
                 new String[] { IDialogConstants.YES_LABEL,
                            IDialogConstants.YES_TO_ALL_LABEL,
                            IDialogConstants.NO_LABEL,
                            IDialogConstants.NO_TO_ALL_LABEL,
                            IDialogConstants.CANCEL_LABEL },
                            0);
        String[] response = new String[] { YES, ALL, NO, NO_ALL, CANCEL };
        //most likely to be called from non-ui thread
        getControl().getDisplay().syncExec(new Runnable() {
            public void run() {
                dialog.open();
            }
        });
        return dialog.getReturnCode() < 0 ? CANCEL : response[dialog
                .getReturnCode()];
    }

    public List<PlatformSupport> getSelectedPlatforms(){
        Object[] checked = getPlatformsGroup().getTableViewer().getCheckedElements();
        ArrayList<PlatformSupport> list = new ArrayList<PlatformSupport>(checked.length);
        for (int i = 0; i < checked.length; i++) {
            list.add((PlatformSupport)checked[i]);
        }
        return list;
    }

    @Override
    protected IContentProvider getPlatformContentProvider() {
        return new ProjectGeneratorContentProvider();
    }

    @Override
    protected IBaseLabelProvider getPlatformLabelProvider() {
        return new ProjectGeneratorLabelProvider();
    }

}
