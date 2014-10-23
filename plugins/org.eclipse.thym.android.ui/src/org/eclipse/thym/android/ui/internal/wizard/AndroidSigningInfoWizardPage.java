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
package org.eclipse.thym.android.ui.internal.wizard;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.thym.ui.wizard.IHybridPlatformWizardPage;

public class AndroidSigningInfoWizardPage extends WizardPage implements IHybridPlatformWizardPage {

    public AndroidSigningInfoWizardPage() {
        super("Android application signing");
        setTitle("Android application signing");
        setDescription("Enter details for signing the application for Android");
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        initializeDialogUnits(container);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(LayoutConstants.getMargins()).applyTo(container);
        //KEY STORE
        createFieldLabel(container, "Key store path:");
        Composite directoryComposite = new Composite(container, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(directoryComposite);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(directoryComposite);
        Combo keyStoreCombo = new Combo(directoryComposite,SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(keyStoreCombo);
        Button btnBrowse = new Button(directoryComposite, SWT.PUSH);
        btnBrowse.setText("Browse...");
        int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        GridDataFactory.fillDefaults().hint(widthHint,SWT.DEFAULT).applyTo(btnBrowse);
        // KEY PASSWORD
        createFieldLabel(container, "Key store password:");
        Text keyStorePassTxt = new Text(container, SWT.BORDER | SWT.PASSWORD);
        GridDataFactory.fillDefaults().grab(true,false).applyTo(keyStorePassTxt);
        // KEY ALIAS
        createFieldLabel(container, "Key alias:");
        Text keyAliasTxt = new Text(container,SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true,false).applyTo(keyAliasTxt);
        //KEY PASSWORD
        createFieldLabel(container, "Key password:");
        Text keyPassTxt = new Text(container, SWT.BORDER | SWT.PASSWORD);
        GridDataFactory.fillDefaults().grab(true,false).applyTo(keyPassTxt);




        setControl(container);
        Dialog.applyDialogFont(container);
    }


    private Label createFieldLabel(final Composite composite, final String labelText) {
        Label label = new Label(composite, SWT.NONE);
        label.setText(labelText);
        int widthHint = Math.max(LayoutConstants.getMinButtonSize().x, label.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
        GridDataFactory.swtDefaults().hint(widthHint, SWT.DEFAULT).applyTo(label);
        return label;
    }

}
