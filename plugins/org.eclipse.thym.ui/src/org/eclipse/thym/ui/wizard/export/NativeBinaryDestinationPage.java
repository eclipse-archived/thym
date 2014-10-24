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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.extensions.NativeProjectBuilder;
import org.eclipse.thym.ui.internal.wizard.PlatformPageWizard;
import org.eclipse.ui.dialogs.IOverwriteQuery;

public class NativeBinaryDestinationPage extends BaseExportWizardDestinationPage implements
        IOverwriteQuery, Listener {

    private Button signCheckbox;

    protected NativeBinaryDestinationPage(String pageName,
            IStructuredSelection selection) {
        super(pageName, selection);
        setTitle(pageName);
        setDescription("Builds a mobile application that can be installed and run on a mobile device");
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        getPlatformsGroup().getTableViewer().setInput(HybridCore.getNativeProjectBuilders());
        Composite container = (Composite)getControl();

        Group optionsGroup = new Group(container, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(optionsGroup);
        optionsGroup.setText("Options");
        GridLayoutFactory.fillDefaults().applyTo(optionsGroup);
        signCheckbox = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        signCheckbox.setText("Generate signed application package");
        signCheckbox.addListener(SWT.Selection, this);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(signCheckbox);

        Dialog.applyDialogFont(getControl());
    }


    @Override
    public String queryOverwrite(String pathString) {

        final MessageDialog dialog = new MessageDialog(getShell(),
                "Overwrite Files?",
                null,
                 pathString+ " already exists. Would you like to overwrite it?",
                 MessageDialog.QUESTION,
                 new String[] { IDialogConstants.YES_LABEL,
                            IDialogConstants.NO_LABEL,
                            IDialogConstants.CANCEL_LABEL },
                            0);
        String[] response = new String[] { YES,  NO, CANCEL };
        //most likely to be called from non-ui thread
        getControl().getDisplay().syncExec(new Runnable() {
            public void run() {
                dialog.open();
            }
        });
        return dialog.getReturnCode() < 0 ? CANCEL : response[dialog
                .getReturnCode()];
    }

    @Override
    protected IContentProvider getPlatformContentProvider() {
        return new NativeProjectBuilderContentProvider();
    }

    @Override
    protected IBaseLabelProvider getPlatformLabelProvider() {
        return new NativeProjectBuilderLabelProvider();
    }

    public List<NativeProjectBuilder> getSelectedPlatforms(){
        Object[] checked = getPlatformsGroup().getTableViewer().getCheckedElements();
        ArrayList<NativeProjectBuilder> builders = new ArrayList<NativeProjectBuilder>(checked.length);
        for (int i = 0; i < checked.length; i++) {
            builders.add((NativeProjectBuilder)checked[i]);
        }
        return builders;
    }

    public boolean isSigned()
    {
    	return signCheckbox.getSelection();
    }

    @Override
    protected void platformSelectionChanged() {
        super.platformSelectionChanged();
        PlatformPageWizard pw = (PlatformPageWizard) getWizard();
        List<NativeProjectBuilder> builders = getSelectedPlatforms();
        String[] platformIDs = new String[builders.size()];
        for (int i = 0; i< builders.size(); i++) {
            platformIDs[i] = builders.get(i).getPlatformID();
        }
        pw.selectPlatformPages(platformIDs);
    }

    @Override
    public void handleEvent(Event event) {
        if (event.widget == signCheckbox) {
            PlatformPageWizard pw = (PlatformPageWizard) getWizard();
            if (signCheckbox.getSelection()) {
                pw.showPlatformPages(this);
            }
            else{
                pw.doNotShowPlatformPages();
            }
        }
    }

}
