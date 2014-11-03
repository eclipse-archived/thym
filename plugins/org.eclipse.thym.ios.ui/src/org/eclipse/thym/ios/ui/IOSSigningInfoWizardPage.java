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
package org.eclipse.thym.ios.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.thym.ios.core.xcode.XCodeBuild;
import org.eclipse.thym.ui.wizard.IHybridPlatformWizardPage;
import org.eclipse.ui.statushandlers.StatusManager;

public class IOSSigningInfoWizardPage extends WizardPage implements
		IHybridPlatformWizardPage, Listener {

	private static final String SETTING_PROVISION_PATH_HISTORY = "ios.provisionPathHistory";
	private static final int PROVISION_PATH_HISTORY_LENGTH = 5;

	
	private Button btnBrowse;
	private Combo provCombo;
	private ComboViewer identityCombo;
	private String[] provisionPathHistory;

	public IOSSigningInfoWizardPage() {
		super("iOS application signing");
        setTitle("iOS application signing");
        setDescription("Enter details for signing the application for iOS devices");
	}

	@Override
	public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        initializeDialogUnits(container);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(LayoutConstants.getMargins()).applyTo(container);
        // Identity
        createFieldLabel(container, "Identity:");
        identityCombo = new ComboViewer(container, SWT.READ_ONLY);
        identityCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete(validatePage());
			}
		});
        identityCombo.setContentProvider(new IStructuredContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
			@Override
			public void dispose() {
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				XCodeBuild build = new XCodeBuild();
				List<String> identities;
				try {
					identities = build.findCodesigningIdentity();
					return identities.toArray(new String[identities.size()]);
				} catch (CoreException e) {
					StatusManager.getManager().handle(e, IOSUI.PLUGIN_ID);
				}
				return new String[0];
			}
		});
        
        GridDataFactory.fillDefaults().grab(true,false).applyTo(identityCombo.getControl());
        // Mobile provision
        createFieldLabel(container, "Provisioning profile:");
        Composite directoryComposite = new Composite(container, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(directoryComposite);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(directoryComposite);
        provCombo = new Combo(directoryComposite,SWT.NONE);
        provCombo.addListener(SWT.Modify, this);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(provCombo);
        btnBrowse = new Button(directoryComposite, SWT.PUSH);
        btnBrowse.setText("Browse...");
        btnBrowse.addListener(SWT.Selection, this);

        
 
        setControl(container);
        identityCombo.getCombo().setFocus();
        identityCombo.setInput(new Object());
        Dialog.applyDialogFont(container);
        setPageComplete(false);
        restoreWidgets();
	}

	@Override
	public Map<String, Object> getValues() {
		Map<String, Object> vals = new HashMap<String, Object>();
		IStructuredSelection sel = (IStructuredSelection) identityCombo.getSelection();
		vals.put("ios.identity", sel.getFirstElement());
		vals.put("ios.provisionPath",provCombo.getText());
		return vals;
	}

	@Override
	public boolean finish() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			if (provisionPathHistory == null) {
				provisionPathHistory = new String[0];
			}
			ArrayList<String> l = new ArrayList<String>(
					Arrays.asList(provisionPathHistory));
			String prov = provCombo.getText();
			l.remove(prov);
			l.add(prov);
			if (l.size() > PROVISION_PATH_HISTORY_LENGTH) {
				l.remove(PROVISION_PATH_HISTORY_LENGTH);
			}
			provisionPathHistory = l.toArray(new String[l.size()]);
			settings.put(SETTING_PROVISION_PATH_HISTORY, provisionPathHistory);
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
		
	}
	
	 private void selectFile(){
	        FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
	        fileDialog.setText("Select a mobile provisoning file");
	        fileDialog.setFilterExtensions(new String[]{"mobileprovision"});
	        String path = fileDialog.open();
	        if(path != null ){
	            provCombo.setText(path);
	        }
	        setPageComplete(validatePage());

	    }
	    private void restoreWidgets() {
	    	IDialogSettings settings = getDialogSettings();
	    	if(settings == null ) return;
	    	provisionPathHistory = settings.getArray(SETTING_PROVISION_PATH_HISTORY);
	    	if(provisionPathHistory != null ){
	    		for (int i = 0; i < provisionPathHistory.length; i++) {
	    			provCombo.add(provisionPathHistory[i], i);
	    	    }
	    	        if(provCombo.getItemCount()>0){
	    	            provCombo.select(0);
	    	        }
	    	}
	    }
	private boolean validatePage() {
		if(identityCombo.getSelection().isEmpty()){
			setErrorMessage("Select and identity");
			return false;
		}
		if(provCombo.getText().isEmpty()){
			setErrorMessage("Please specify a mobile provisioning profile file");
			return false;
		}
		setErrorMessage(null);
		return true;
	}

}
