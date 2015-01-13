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
package org.eclipse.thym.ui.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
/**
 * UI widget for selecting a directory. Widget also manages the history of selected 
 * directories. 
 * 
 * @author Gorkem Ercan
 *
 */
public class DirectorySelectionGroup extends Group {

    private static final String SETTINGS_KEY_DESTINATION_HISTORY = "destinationHistory";
    private static final int DESTINATION_HISTORY_LENGTH = 5;
    private Combo destinationCombo;
    private String[] destinationHistory;
    private String fallback;

    public DirectorySelectionGroup(Composite parent, int style) {
        super(parent, style);
        createGroup();
    }

    private void createGroup() {
        setLayout(new GridLayout(3, false));
        final Label lblDirectory = new Label(this, SWT.NONE);
        lblDirectory.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblDirectory.setText("Directory:");

        destinationCombo = new Combo(this, SWT.NONE);
        destinationCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                sendModifyEvent();
            }
        });
        destinationCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        final Button btnBrowse = new Button(this, SWT.PUSH);
        btnBrowse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                chooseDirectory();
            }
        });
        btnBrowse.setText("Browse...");
    }

    @Override
    protected void checkSubclass() {
    }

    private void sendModifyEvent(){
        Event e = new Event();
        e.widget=this;
        e.text=this.destinationCombo.getText();
        notifyListeners(SWT.Modify, e);
    }

    private void chooseDirectory(){
        final DirectoryDialog dialog = new DirectoryDialog(this.getShell());
        dialog.setText("Select Destination");
        dialog.setMessage("Select a destination directory");
        String directory = dialog.open();
        if(directory != null ){
            this.destinationCombo.setText(directory);
            sendModifyEvent();
        }
    }
    
    /**
     * Sets the values to the internal Combo widget.
     * A null or empty array will clear the values.
     *
     * @param newValues
     */
    public void setComboValues(String... newValues){
        destinationCombo.removeAll();
        if(newValues == null )
            return;
        for (int i = 0; i < newValues.length; i++) {
            destinationCombo.add(newValues[i], i);
        }
        if(destinationCombo.getItemCount()>0){
            destinationCombo.select(0);
        }
    }
    /**
     * Returns the value on the directory field.
     * @return directory
     */
    public String getValue(){
        return destinationCombo.getText();
    }

    /**
     * Saves the history of selected directories to given dialog settings
     * 
     * @param settings
     */
    public void saveHistory(final IDialogSettings settings){
        if(settings == null ) return;
        if(destinationHistory == null ){
            destinationHistory = new String[0];
        }
        ArrayList<String> l = new ArrayList<String>(Arrays.asList(destinationHistory));
        String directory = destinationCombo.getText();
        l.remove(directory);
        l.add(directory);
        if(l.size()>DESTINATION_HISTORY_LENGTH){
            l.remove(DESTINATION_HISTORY_LENGTH);
        }
        destinationHistory = l.toArray(new String[l.size()]);
        setComboValues(destinationHistory);
        settings.put(SETTINGS_KEY_DESTINATION_HISTORY, this.destinationHistory);

    }

    /**
     * Restores the history from dialog settings 
     * @param settings
     */
    public void restoreHistory(final IDialogSettings settings){
        if ( settings == null) {
        	if(fallback != null ){
        		destinationHistory = new String[] {fallback};
        	}
        }else{
        	String[] history = settings.getArray(SETTINGS_KEY_DESTINATION_HISTORY);
        	if(history != null ){
        		destinationHistory =history;
        	}
        }
        setComboValues(destinationHistory);
    }
   
    /**
     * Sets a default value to be used in case the history is empty.
     * @param defaultValue
     */
    public void setDefaultValue(String defaultValue){
    	this.fallback = defaultValue;
    }

    /**
     * Utility to check valid directory.
     * 
     * @param dstFile
     * @return
     */
    public static boolean isValidDirectory(File dstFile) {
    	if(dstFile == null )return false;
        try {
            if(dstFile.getCanonicalPath().isEmpty()){
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}
