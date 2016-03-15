/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Red Hat Inc. - initial API and implementation and/or initial documentation
 *		Zend Technologies Ltd. - initial implementation
 *		IBM Corporation - initial API and implementation
 *******************************************************************************/  

package org.eclipse.thym.win.internal.ui.launch;

import java.util.Map;
import java.util.TreeMap;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.thym.core.HybridCore;
import org.eclipse.thym.core.HybridProject;
import org.eclipse.thym.core.HybridProjectLaunchConfigConstants;
import org.eclipse.thym.win.core.WinCore;
import org.eclipse.thym.win.core.vstudio.WinConstants;
import org.eclipse.thym.win.internal.ui.Messages;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class WinOptionsTab extends AbstractLaunchConfigurationTab {

	private static final String DEFAULT_EMULATOR = Messages.WinOptionsTab_DefaultEmulator;

	private Text projectText;
	private Listener dirtyListener;
	private Combo devicesCombo;

	private Map<String, Integer> devices;

	private class DirtyListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			setDirty(true);
			updateLaunchConfigurationDialog();
		}
	}

	public WinOptionsTab() {
		this.dirtyListener = new DirtyListener();
		this.devices = new TreeMap<String, Integer>();
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		comp.setLayout(new GridLayout(1, false));

		Group projectGroup = new Group(comp, SWT.NONE);
		projectGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		projectGroup.setText(Messages.WinOptionsTab_ProjectGroup);
		projectGroup.setLayout(new GridLayout(3, false));

		Label projectLabel = new Label(projectGroup, SWT.NONE);
		projectLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		projectLabel.setText(Messages.WinOptionsTab_ProjectLabel);

		projectText = new Text(projectGroup, SWT.BORDER);
		projectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		projectText.addListener(SWT.Modify, dirtyListener);

		Button browseButton = new Button(projectGroup, SWT.NONE);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(
						getShell(), WorkbenchLabelProvider
								.getDecoratingWorkbenchLabelProvider());
				dialog.setTitle(Messages.WinOptionsTab_ProjectSelection);
				dialog.setMessage(Messages.WinOptionsTab_SelectonDesc);
				dialog.setElements(HybridCore.getHybridProjects().toArray());
				if (dialog.open() == Window.OK) {
					HybridProject project = (HybridProject) dialog
							.getFirstResult();
					projectText.setText(project.getProject().getName());
				}
			}
		});
		browseButton.setText(Messages.WinOptionsTab_BrowseLabel);

		Group emulatorGroup = new Group(comp, SWT.NONE);
		emulatorGroup.setLayout(new GridLayout(2, false));
		emulatorGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		emulatorGroup.setText(Messages.WinOptionsTab_EmulatorGroup);

		Label deviceLabel = new Label(emulatorGroup, SWT.NONE);
		deviceLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		deviceLabel.setText(Messages.WinOptionsTab_DeviceName);

		devicesCombo = new Combo(emulatorGroup, SWT.READ_ONLY);
		devicesCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		devicesCombo.addListener(SWT.Selection, dirtyListener);

		if (devices != null && !devices.isEmpty()) {
			devicesCombo.add(DEFAULT_EMULATOR);
		}
	}

	@Override
	public String getName() {
		return Messages.WinOptionsTab_TabName;
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String projectName = null;
			projectName = configuration.getAttribute(
					HybridProjectLaunchConfigConstants.ATTR_BUILD_SCOPE,
					(String) null);
			if (projectName != null) {
				projectText.setText(projectName);
			}

			setDirty(false);
		} catch (CoreException e) {
			WinCore.log(
					IStatus.ERROR,
					"Could not initialize launch configuration for Windows Phone 8 Emulator", //$NON-NLS-1$
					e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
				HybridProjectLaunchConfigConstants.ATTR_BUILD_SCOPE,
				projectText.getText());
		String device = devicesCombo.getText();
		if (device != null && !device.isEmpty()) {
			int id = -1;
			if (!DEFAULT_EMULATOR.equals(device)) {
				id = devices.get(device);
			}
			configuration.setAttribute(WinConstants.ATTR_DEVICE_IDENTIFIER, id);
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		if (devices != null && !devices.isEmpty()) {
			devicesCombo.select(devicesCombo.indexOf(DEFAULT_EMULATOR));
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		return isTabValid() && super.isValid(launchConfig);
	}

	@Override
	public boolean canSave() {
		return isTabValid();
	}

	/**
	 * Validate all fields on the tab.
	 * 
	 * @return <code>true</code> if all fields on the tab are valid; otherwise
	 *         return <code>false</code>
	 */
	private boolean isTabValid() {
		setMessage(Messages.WinOptionsTab_Description);
		setErrorMessage(null);
		// firstly check if there are emulators in the SDK
		if (devices.isEmpty()) {
			setErrorMessage(Messages.WinOptionsTab_NoEmulatorsError);
			return false;
		}
		String projectName = projectText.getText();
		if (projectName.isEmpty()) {
			return false;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(projectName);
		if (!project.exists()) {
			return false;
		}

		String device = devicesCombo.getText();
		if (device.isEmpty()) {
			return false;
		}
		return true;
	}

}
