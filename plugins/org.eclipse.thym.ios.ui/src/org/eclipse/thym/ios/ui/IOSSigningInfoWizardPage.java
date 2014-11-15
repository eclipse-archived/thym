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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.thym.ios.core.xcode.ProvisioningProfile;
import org.eclipse.thym.ios.core.xcode.XCodeBuild;
import org.eclipse.thym.ui.wizard.IHybridPlatformWizardPage;
import org.eclipse.ui.statushandlers.StatusManager;

public class IOSSigningInfoWizardPage extends WizardPage implements
		IHybridPlatformWizardPage, ISelectionChangedListener{


	
	private ComboViewer provCombo;
	private ComboViewer identityCombo;
	
	
	private static class ProvisionProfileLabelProvider extends BaseLabelProvider implements ILabelProvider{

		@Override
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public String getText(Object element) {
			ProvisioningProfile profile = (ProvisioningProfile) element;
			return profile.getName();
		}
		
	}

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
        identityCombo.addSelectionChangedListener(this);
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
        provCombo = new ComboViewer(container, SWT.READ_ONLY);
        provCombo.addSelectionChangedListener(this);
        provCombo.setContentProvider(new IStructuredContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
			@Override
			public void dispose() {
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				XCodeBuild build = new XCodeBuild();
				List<ProvisioningProfile> profiles;
				try{
					profiles = build.findProvisioningProfiles();
					return profiles.toArray(new ProvisioningProfile[profiles.size()]);
				}catch (CoreException e){
					StatusManager.getManager().handle(e, IOSUI.PLUGIN_ID);
				}
				return new String[0];
			}
		});
        provCombo.setLabelProvider(new ProvisionProfileLabelProvider());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(provCombo.getControl());

        
 
        setControl(container);
        identityCombo.setInput(new Object());
        provCombo.setInput(new Object());
        Dialog.applyDialogFont(container);
        setPageComplete(false);
        restoreWidgets();
	}

	@Override
	public Map<String, Object> getValues() {
		Map<String, Object> vals = new HashMap<String, Object>();
		IStructuredSelection identitySel = (IStructuredSelection) identityCombo.getSelection();
		IStructuredSelection provSel = (IStructuredSelection) provCombo.getSelection();
		vals.put("ios.identity", identitySel.getFirstElement());
		vals.put("ios.provision",((ProvisioningProfile)provSel.getFirstElement()).getUUID());
		return vals;
	}

	@Override
	public boolean finish() {
		return true;
	}
	
    private Label createFieldLabel(final Composite composite, final String labelText) {
        Label label = new Label(composite, SWT.NONE);
        label.setText(labelText);
        int widthHint = Math.max(LayoutConstants.getMinButtonSize().x, label.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
        GridDataFactory.swtDefaults().hint(widthHint, SWT.DEFAULT).applyTo(label);
        return label;
    }

	
	private void restoreWidgets() {
		
	}
	
	private boolean validatePage() {
		if(identityCombo.getSelection().isEmpty()){
			setErrorMessage("Please select an identity");
			return false;
		}
		if(provCombo.getSelection().isEmpty()){
			setErrorMessage("Please select a mobile provisioning profile");
			return false;
		}
		setErrorMessage(null);
		return true;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		setPageComplete(validatePage());	
	}

}
