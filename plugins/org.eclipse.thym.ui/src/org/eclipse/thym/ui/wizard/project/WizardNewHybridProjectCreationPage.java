/*******************************************************************************
 * Copyright (c) 2013, 2017 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.thym.ui.wizard.project;

import static org.eclipse.jface.dialogs.Dialog.DLG_IMG_MESSAGE_WARNING;
import static org.eclipse.jface.dialogs.Dialog.DLG_IMG_MESSAGE_ERROR;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.dialogs.WorkingSetGroup;
import org.osgi.framework.Version;
import org.eclipse.thym.core.HybridProjectConventions;
import org.eclipse.thym.core.internal.cordova.CordovaCLI;
import org.eclipse.thym.core.internal.cordova.CordovaCLIErrors;
import org.eclipse.thym.core.internal.cordova.ErrorDetectingCLIResult;
import org.eclipse.thym.ui.HybridUI;

public class WizardNewHybridProjectCreationPage extends WizardNewProjectCreationPage{
	private Text txtName;
	private Text txtID;
	private WorkingSetGroup workingSetGroup;
	private final PropertyModifyListener propertyModifyListener = new PropertyModifyListener();
	private IStructuredSelection currentSelection;
	private CordovaIsAvailableJob cordovaIsAvailableJob;
	private CLabel cordovaLabel;
	private Boolean cordovaFound = null;
	
	class PropertyModifyListener implements ModifyListener{
		private boolean skipValidation = false;
		private boolean changed =false;
		@Override
		public void modifyText(ModifyEvent e) {
			if(!skipValidation){
				if(!changed && (e.widget == txtID || e.widget == txtName) ){
					changed =true;
				}
				setPageComplete(validatePage());	
			}
		}
		
		public void setSkipValidation(boolean skipValidation) {
			this.skipValidation = skipValidation;
		}
		
		public boolean isNameOrIDChanged(){
			return changed;
		}
	}
	

	public WizardNewHybridProjectCreationPage(String pageName, IStructuredSelection selection) {
		super(pageName);
		this.currentSelection = selection;
		setTitle("Create Hybrid Mobile Application Project");
		setDescription("Create a hybrid mobile application using Apache Cordova for cross-platform mobile development");
	}

	public void createControl(Composite parent ){
        super.createControl(parent);
         
        Group applicationGroup = new Group((Composite)getControl(), SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        applicationGroup.setLayout(layout);
        applicationGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        applicationGroup.setText("Mobile Application");
        
        Label lblName = new Label(applicationGroup, SWT.NONE);
        lblName.setText("Name:");
        
        
        txtName = new Text(applicationGroup, SWT.BORDER);
        txtName.addModifyListener(propertyModifyListener);
        txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtName.setMessage("a friendly name");
        
        Label lblId = new Label(applicationGroup, SWT.NONE);
        lblId.setText("ID:");
        
        txtID = new Text(applicationGroup, SWT.BORDER);
        txtID.setMessage("com.mycom.app");
        txtID.addModifyListener(propertyModifyListener);
        txtID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        createWorkingSetGroup();
        
        Group cordovaGroup = new Group((Composite)getControl(), SWT.NONE);
        cordovaGroup.setText("Cordova version");
        layout = new GridLayout();
        layout.numColumns = 1;
        cordovaGroup.setLayout(layout);
        cordovaGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        cordovaLabel = new CLabel(cordovaGroup, SWT.NONE);
		cordovaLabel.setImage(JFaceResources.getImage(DLG_IMG_MESSAGE_WARNING));
		cordovaLabel.setText("Checking cordova availability...");
		cordovaLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true, true, 1, 1));

        setErrorMessage(null);
        setMessage(null);
        setPageComplete(validatePage());
        Dialog.applyDialogFont(getControl());
        checkCordovaIsAvailable();
	}
	
	@Override
	protected boolean validatePage() {
		if(cordovaFound == null){
			setMessage("Checking cordova availability...", WARNING);
			this.getContainer().updateMessage();
			return false;
		}
		if(!cordovaFound){
			setMessage(ErrorDetectingCLIResult.CORDOVA_NOT_FOUND, ERROR);
			this.getContainer().updateMessage();
			return false;
		}
		
		boolean superValidate = super.validatePage();
		if(!superValidate || txtID == null || txtName == null ){//validate is actually called first time on super.createControl()
			return superValidate;                                // in order to avoid NPEs for the half initialized UI we do a partial
		}                                                       // until all UI components are in place.
		if( !propertyModifyListener.isNameOrIDChanged() ){
			String id = HybridProjectConventions.generateProjectID(getProjectName());
			String name = HybridProjectConventions.generateApplicationName(getProjectName());
			propertyModifyListener.setSkipValidation(true);
			if(id != null ){
				txtID.setText(id);
			}
			txtName.setText(name);
			propertyModifyListener.setSkipValidation(false);
		}
		
		IStatus status1 = HybridProjectConventions.validateApplicationName(txtName.getText());
		IStatus status2 = HybridProjectConventions.validateProjectID(txtID.getText());
		IStatus status = null;
		
		//Interested on ERROR and WARNINGS with 
		//ERRORs getting priority over WARNINGs
		if(status1.matches(IStatus.ERROR)){
			status = status1;
		}
		if(status2.matches(IStatus.ERROR)){
			status = status2;
		}
		
		if(status1.matches(IStatus.WARNING)){
			status = status1;
		}
		if(status2.matches(IStatus.WARNING)){
			status = status2;
		}
		
		if (status == null ){
			setErrorMessage(null);
			setMessage(null);
			return true;
		}
		
		if(status.getSeverity() == IStatus.ERROR ){
			setMessage(status.getMessage(), ERROR);
			this.getContainer().updateMessage();
			return false;
		}
		if (status.getSeverity() == IStatus.WARNING) {
			setMessage(status.getMessage(), WARNING);
			this.getContainer().updateMessage();
			return true;
		}
		return true;
	}
	
	private void createWorkingSetGroup() {
		String[] workingSetIds = new String[] {"org.eclipse.ui.resourceWorkingSetPage",  //$NON-NLS-1$
		"org.eclipse.wst.jsdt.ui.JavaWorkingSetPage"};  //$NON-NLS-1$
    	workingSetGroup = new WorkingSetGroup((Composite)getControl(), currentSelection, workingSetIds);
	}
	
	public String getApplicationName(){
		return txtName.getText();
	}
	
	public String getApplicationID(){
		return txtID.getText();
	}
	
	public IWorkingSet[] getSelectedWorkingSets(){
		return workingSetGroup.getSelectedWorkingSets();
	}
	
	class CordovaIsAvailableJob extends Job {
		
		private String cordovaVersion;
		
		public CordovaIsAvailableJob() {
			super("Checking cordova availability");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				ErrorDetectingCLIResult result = new CordovaCLI().version(monitor).convertTo(ErrorDetectingCLIResult.class);
				if(result.asStatus().getCode() == CordovaCLIErrors.ERROR_COMMAND_MISSING){
					return Status.OK_STATUS;
				}
				if(result.asStatus().isOK()){
					cordovaVersion = Version.parseVersion(result.getMessage()).toString();
					return Status.OK_STATUS;
				}
			} catch (Exception e) {
				HybridUI.log(WARNING, "Unable to determine if cordova is available", e);
			}
			return new Status(Status.WARNING, HybridUI.PLUGIN_ID, "");
			
		}
		
		public String getCordovaVersion(){
			return cordovaVersion;
		}

	}

	private void checkCordovaIsAvailable() {
		cordovaIsAvailableJob = new CordovaIsAvailableJob();
		cordovaIsAvailableJob.setUser(true);
		cordovaIsAvailableJob.schedule();
		cordovaIsAvailableJob.addJobChangeListener(new JobChangeAdapter() {
			
			@Override
			public void done(IJobChangeEvent event) {
				if(!getControl().isDisposed()){
					final Display display = getControl().getDisplay();
					final int severity = event.getJob().getResult().getSeverity();
					display.syncExec(new Runnable() {
						@Override
						public void run() {
							if(severity == Status.OK){
								if(cordovaIsAvailableJob.getCordovaVersion() != null){
									updateCordovaLabel(cordovaIsAvailableJob.getCordovaVersion(), null, null, true);
								} else {
									updateCordovaLabel(ErrorDetectingCLIResult.CORDOVA_NOT_FOUND, 
											JFaceResources.getImage(DLG_IMG_MESSAGE_ERROR), 
											getControl().getDisplay().getSystemColor(SWT.COLOR_RED), false);
								}
							} else {
								updateCordovaLabel("Unable to determine if cordova is available", 
										JFaceResources.getImage(DLG_IMG_MESSAGE_WARNING), null, true);
							}
						}
						
						private void updateCordovaLabel(String text, Image image, Color foreground , boolean found){
							cordovaLabel.setText(text);
							cordovaLabel.setImage(image);
							cordovaLabel.setForeground(foreground);
							cordovaFound = found;
							setPageComplete(validatePage());
						}
					});
				}
			}
		});
	}
	
	
}
