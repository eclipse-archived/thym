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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.thym.ui.wizard.IHybridPlatformWizardPage;

public class AndroidSigningInfoWizardPage extends WizardPage implements IHybridPlatformWizardPage,Listener {

	private static final String SETTING_KEYSTORE_HISTORY = "android.keystoreHistory";
	private static final int DESTINATION_HISTORY_LENGTH = 5;

    private Combo keyStoreCombo;
    private Text keyStorePassTxt;
    private Text keyAliasTxt;
    private Text keyPassTxt;
    private Button btnBrowse;
    private String[] keyStoreHistory;

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
        keyStoreCombo = new Combo(directoryComposite,SWT.NONE);
        keyStoreCombo.addListener(SWT.Modify, this);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(keyStoreCombo);
        btnBrowse = new Button(directoryComposite, SWT.PUSH);
        btnBrowse.setText("Browse...");
        btnBrowse.addListener(SWT.Selection, this);

        int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        GridDataFactory.fillDefaults().hint(widthHint,SWT.DEFAULT).applyTo(btnBrowse);
        // KEY PASSWORD
        createFieldLabel(container, "Key store password:");
        keyStorePassTxt = new Text(container, SWT.BORDER | SWT.PASSWORD);
        keyStorePassTxt.addListener(SWT.Modify, this);
        GridDataFactory.fillDefaults().grab(true,false).applyTo(keyStorePassTxt);
        // KEY ALIAS
        createFieldLabel(container, "Key alias:");
        keyAliasTxt = new Text(container,SWT.BORDER);
        keyAliasTxt.addListener(SWT.Modify, this);
        GridDataFactory.fillDefaults().grab(true,false).applyTo(keyAliasTxt);
        //KEY PASSWORD
        createFieldLabel(container, "Key password:");
        keyPassTxt = new Text(container, SWT.BORDER | SWT.PASSWORD);
        keyPassTxt.addListener(SWT.Modify, this);
        GridDataFactory.fillDefaults().grab(true,false).applyTo(keyPassTxt);

        setControl(container);
        Dialog.applyDialogFont(container);
        restoreWidgets();
        setPageComplete(false);
    }


    private void restoreWidgets() {
    	IDialogSettings settings = getDialogSettings();
    	if(settings == null ) return;
    	keyStoreHistory = settings.getArray(SETTING_KEYSTORE_HISTORY);
    	if(keyStoreHistory != null ){
    		for (int i = 0; i < keyStoreHistory.length; i++) {
    			keyStoreCombo.add(keyStoreHistory[i], i);
    	    }
    	        if(keyStoreCombo.getItemCount()>0){
    	            keyStoreCombo.select(0);
    	        }
    	}
    }

	@Override
    public Map<String, Object> getValues() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("key.store", keyStoreCombo.getText());
        values.put("key.store.password", keyStorePassTxt.getText());
        values.put("key.alias", keyAliasTxt.getText());
        values.put("key.alias.password", keyPassTxt.getText());
        return values;
    }

    @Override
	public boolean finish() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			if (keyStoreHistory == null) {
				keyStoreHistory = new String[0];
			}
			ArrayList<String> l = new ArrayList<String>(
					Arrays.asList(keyStoreHistory));
			String keystore = keyStoreCombo.getText();
			l.remove(keystore);
			l.add(keystore);
			if (l.size() > DESTINATION_HISTORY_LENGTH) {
				l.remove(DESTINATION_HISTORY_LENGTH);
			}
			keyStoreHistory = l.toArray(new String[l.size()]);
			settings.put(SETTING_KEYSTORE_HISTORY, keyStoreHistory);
		}
		return true;
	}
    private Label createFieldLabel(final Composite composite, final String labelText) {
        Label label = new Label(composite, SWT.NONE);
        label.setText(labelText);
        int widthHint = Math.max(LayoutConstants.getMinButtonSize().x, label.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
        GridDataFactory.swtDefaults().hint(widthHint, SWT.DEFAULT).applyTo(label);
        return label;
    }

    @Override
    public void handleEvent(Event event) {
        if(event.type == SWT.Selection && event.widget == btnBrowse){
            selectFile();
        }
        if(event.type == SWT.Modify){
        	setPageComplete(validatePage());
        }
    }
    private boolean validatePage(){
    	if(keyStoreCombo.getText().isEmpty()){
    		setErrorMessage("Specify a Java key store");
    		return false;
    	}
    	if(keyStorePassTxt.getText().isEmpty()){
    		setErrorMessage("Specify a key stroe password");
    		return false;
    	}
    	if(keyAliasTxt.getText().isEmpty()){
    		setErrorMessage("Specify a key alias");
    		return false;
    	}
    	if(keyPassTxt.getText().isEmpty()){
    		setErrorMessage("Specify a password for the key");
    		return false;
    	}
    	setErrorMessage(null);
    	return true;
    }

    private void selectFile(){
        FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
        fileDialog.setText("Select a key store");
        String path = fileDialog.open();
        if(path != null ){
            keyStoreCombo.setText(path);
        }
        setPageComplete(validatePage());

    }



}
